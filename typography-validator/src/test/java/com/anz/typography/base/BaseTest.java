package com.anz.typography.base;

import com.microsoft.playwright.*;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * Base test class — manages Playwright lifecycle.
 * One Browser instance per suite; each test gets its own BrowserContext + Page.
 */
public class BaseTest {

    // ── Shared across the entire suite ────────────────────────────────────────
    protected static Playwright playwright;
    protected static Browser    browser;

    // ── Per-test (thread-safe via ThreadLocal) ────────────────────────────────
    private static final ThreadLocal<BrowserContext> contextHolder = new ThreadLocal<>();
    private static final ThreadLocal<Page>           pageHolder    = new ThreadLocal<>();

    @BeforeSuite(alwaysRun = true)
    public void launchBrowser() {
        playwright = Playwright.create();

        // Launch Chromium in headed or headless mode
        boolean headless = Boolean.parseBoolean(
            System.getProperty("headless", "true")
        );

        browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setArgs(java.util.Arrays.asList(
                    "--disable-web-security",
                    "--no-sandbox",
                    "--start-maximized"
                ))
        );
    }

    @AfterSuite(alwaysRun = true)
    public void closeBrowser() {
        if (browser    != null) browser.close();
        if (playwright != null) playwright.close();
    }

    // ─── Create a fresh context + page for each test thread ───────────────────
    protected Page createPage() {
        BrowserContext context = browser.newContext(
            new Browser.NewContextOptions()
                .setViewportSize(1440, 900)   // Desktop viewport
                .setIgnoreHTTPSErrors(true)
        );
        Page page = context.newPage();

        contextHolder.set(context);
        pageHolder.set(page);

        return page;
    }

    protected void closePage() {
        Page page = pageHolder.get();
        if (page != null) {
            page.close();
            pageHolder.remove();
        }
        BrowserContext ctx = contextHolder.get();
        if (ctx != null) {
            ctx.close();
            contextHolder.remove();
        }
    }
}