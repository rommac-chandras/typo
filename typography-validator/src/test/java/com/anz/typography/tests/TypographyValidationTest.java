package com.anz.typography.tests;

import com.anz.typography.base.BaseTest;
import com.anz.typography.model.ElementResult;
import com.anz.typography.model.TagResult;
import com.anz.typography.model.ValidationReport;
import com.anz.typography.pages.TypographyPage;
import com.anz.typography.utils.ReportGenerator;
import com.anz.typography.utils.UrlProvider;
import com.microsoft.playwright.Page;

import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.Map;

/**
 * Main test class — one test method per URL.
 * Each invocation (one per URL) = one TestNG test case result.
 *
 * Run all URLs:   mvn test
 * Run headless:   mvn test -Dheadless=true
 * Run headed:     mvn test -Dheadless=false
 */
@Epic("ANZ Design System")
@Feature("Typography Validation")
public class TypographyValidationTest extends BaseTest {

    // ─── DataProvider: feeds URLs from urls.txt ───────────────────────────────
    @DataProvider(name = "pages", parallel = false)
    public Object[][] pageUrls() {
        return UrlProvider.getUrlDataProvider();
    }

    // ─── Main Test — one execution per URL ───────────────────────────────────
    @Test(
        dataProvider  = "pages",
        description   = "Validate typography specifications on each page",
        groups        = { "typography", "desktop" }
    )
    @Story("Desktop Typography Specs")
    @Severity(SeverityLevel.NORMAL)
    public void validatePageTypography(int pageNumber, String url) {

        // ── Test name shown in reports
        String testName = "Page " + pageNumber + " — " + url;

        Page           page     = createPage();
        TypographyPage typoPage = new TypographyPage(page);

        try {
            // ── Step 1: Navigate ──────────────────────────────────────────────
            navigateStep(typoPage, url);

            // ── Step 2: Validate ──────────────────────────────────────────────
            ValidationReport report = validateStep(typoPage, url);

            // ── Step 3: Log to console ─────────────────────────────────────────
            String consoleOutput = ReportGenerator.buildConsoleReport(report);
            System.out.println(consoleOutput);

            // Attach console report to Allure
            Allure.addAttachment("Typography Report", "text/plain", consoleOutput);

            // ── Step 4: Save HTML report ───────────────────────────────────────
            String safeFileName = "page-" + pageNumber + "-"
                                + url.replaceAll("[^a-zA-Z0-9]", "_")
                                      .replaceAll("_+", "_")
                                      .replaceAll("^_|_$", "");
            if (safeFileName.length() > 100) {
                safeFileName = safeFileName.substring(0, 100);
            }
            ReportGenerator.writeHtmlReport(report, safeFileName);

            // ── Step 5: Attach screenshot ──────────────────────────────────────
            Allure.addAttachment(
                "Screenshot — " + url,
                "image/png",
                new java.io.ByteArrayInputStream(typoPage.takeScreenshot()),
                "png"
            );

            // ── Step 6: Assert ────────────────────────────────────────────────
            assertTypography(report, testName);

        } finally {
            closePage();
        }
    }

    // ─── Navigate step ────────────────────────────────────────────────────────
    @Step("Navigate to {url}")
    private void navigateStep(TypographyPage typoPage, String url) {
        System.out.println("\n  🔗 Navigating to: " + url);
        typoPage.navigateTo(url);
    }

    // ─── Validate step ────────────────────────────────────────────────────────
    @Step("Validate typography on page")
    private ValidationReport validateStep(TypographyPage typoPage, String url) {
        System.out.println("  🔍 Validating typography...");
        return typoPage.validatePage();
    }

    // ─── Assertion: use SoftAssert so all failures are reported ──────────────
    @Step("Assert typography results")
    private void assertTypography(ValidationReport report, String testName) {
        SoftAssert sa = new SoftAssert();

        for (Map.Entry<String, TagResult> entry
                : report.getTagResults().entrySet()) {

            String    tag = entry.getKey();
            TagResult r   = entry.getValue();

            if (r.hasFailures()) {
                for (ElementResult fi : r.getFailed()) {
                    for (ElementResult.PropertyError err : fi.getErrors()) {

                        String msg = String.format(
                            "[%s] Page: %s | Tag: %s #%d | Component: %s | "
                            + "Property: %s | Expected: %s | Actual: %s",
                            testName,
                            report.getPageUrl(),
                            tag.toUpperCase(),
                            fi.getIndex(),
                            fi.getAemComponent(),
                            err.property,
                            err.expected,
                            err.actual
                        );

                        // Each mismatch = one soft assertion failure
                        sa.fail(msg);
                    }
                }
            }
        }

        // Triggers all collected assertion failures at once
        sa.assertAll();
    }
}