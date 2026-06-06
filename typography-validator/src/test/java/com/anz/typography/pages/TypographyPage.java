package com.anz.typography.pages;

import com.anz.typography.config.TypographySpecs;
import com.anz.typography.config.TypographySpecs.Spec;
import com.anz.typography.model.ElementResult;
import com.anz.typography.model.ElementResult.PropertyError;
import com.anz.typography.model.TagResult;
import com.anz.typography.model.ValidationReport;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Page Object — encapsulates all typography validation logic.
 * Each method maps 1:1 with the original JavaScript console script.
 */
public class TypographyPage {

    private final Page page;

    public TypographyPage(Page page) {
        this.page = page;
    }

    // ─── Navigate to URL ──────────────────────────────────────────────────────
    public void navigateTo(String url) {
        page.navigate(url, new Page.NavigateOptions()
            .setTimeout(60_000)
            .setWaitUntil(com.microsoft.playwright.options.WaitUntilState.NETWORKIDLE));
    }

    // ─── Main entry: validate entire page ────────────────────────────────────
    public ValidationReport validatePage() {
        String url   = page.url();
        String title = page.title();

        ValidationReport report = new ValidationReport(url, title);

        for (Map.Entry<String, Spec> entry : TypographySpecs.DESKTOP_SPECS.entrySet()) {
            String tag  = entry.getKey();
            Spec   spec = entry.getValue();

            TagResult tagResult = validateTag(tag, spec);
            report.addTagResult(tag, tagResult);
        }

        return report;
    }

    // ─── Validate all elements matching one tag ───────────────────────────────
    private TagResult validateTag(String tag, Spec spec) {
        List<ElementResult> passed   = new ArrayList<>();
        List<ElementResult> failed   = new ArrayList<>();
        List<String>        warnings = new ArrayList<>();

        List<ElementHandle> elements = page.querySelectorAll(tag);

        // No elements found → warning
        if (elements.isEmpty()) {
            warnings.add(tag.toUpperCase() + " (" + spec.label
                         + "): No elements found on page");
            return new TagResult(tag, spec.label, passed, failed, warnings);
        }

        for (int i = 0; i < elements.size(); i++) {
            ElementHandle el = elements.get(i);

            try {
                ElementResult result = validateElement(el, i + 1, spec);
                if (result.isPassed()) {
                    passed.add(result);
                } else {
                    failed.add(result);
                }
            } catch (Exception ex) {
                // Element may have become detached — skip safely
                warnings.add("Element #" + (i + 1) + " could not be evaluated: "
                             + ex.getMessage());
            }
        }

        return new TagResult(tag, spec.label, passed, failed, warnings);
    }

    // ─── Validate a single element ────────────────────────────────────────────
    private ElementResult validateElement(ElementHandle el,
                                          int index,
                                          Spec spec) {
        // Grab computed styles via JS evaluation on the element
        @SuppressWarnings("unchecked")
        Map<String, Object> styles = (Map<String, Object>) el.evaluate(
            "el => {\n" +
            "  var s = window.getComputedStyle(el);\n" +
            "  return {\n" +
            "    fontSize:      parseFloat(s.fontSize),\n" +
            "    lineHeight:    parseFloat(s.lineHeight),\n" +
            // Ensure letterSpacing 'normal' -> treat as 0 rather than NaN
            "    letterSpacing: (function(){ var ls = parseFloat(s.letterSpacing); return isNaN(ls) ? 0 : ls; })(),\n" +
            "    fontFamily:    s.fontFamily,\n" +
            "    text:          el.textContent.trim().substring(0, 50) || '(empty)',\n" +
            "    classes:       el.className || ''\n" +
            "  };\n" +
            "}"
        );

        double fontSize      = toDouble(styles.get("fontSize"));
        double lineHeight    = toDouble(styles.get("lineHeight"));
        double letterSpacing = toDouble(styles.get("letterSpacing"));
        String fontFamily    = String.valueOf(styles.get("fontFamily"));
        String text          = String.valueOf(styles.get("text"));
        String classes       = String.valueOf(styles.get("classes"));

        // Resolve AEM component name from element's class hierarchy
        String aemComponent = getAEMComponent(el, classes);

        // Build error list
        List<PropertyError> errors = new ArrayList<>();

        // 1. Font Size
        if (!isWithinTolerance(fontSize, spec.fontSize,
                               TypographySpecs.FONT_SIZE_TOLERANCE)) {
            errors.add(new PropertyError(
                "fontSize",
                spec.fontSize + "px",
                fontSize + "px"
            ));
        }

        // 2. Line Height
        if (!isWithinTolerance(lineHeight, spec.lineHeight,
                               TypographySpecs.LINE_HEIGHT_TOLERANCE)) {
            errors.add(new PropertyError(
                "lineHeight",
                spec.lineHeight + "px",
                lineHeight + "px"
            ));
        }

        // 3. Letter Spacing
        double expectedLetterSpacingPx = spec.expectedLetterSpacingPx();
        if (!isWithinTolerance(letterSpacing, expectedLetterSpacingPx,
                               TypographySpecs.LETTER_SPACING_TOLERANCE)) {
            errors.add(new PropertyError(
                "letterSpacing",
                String.format("%.2fpx", expectedLetterSpacingPx),
                letterSpacing + "px"
            ));
        }

        // 4. Font Family
        if (!isValidFontFamily(fontFamily)) {
            errors.add(new PropertyError(
                "fontFamily",
                "Aeonik, Arial, Helvetica, sans-serif",
                fontFamily
            ));
        }

        return new ElementResult(index, aemComponent, text,
                                 fontSize, lineHeight, letterSpacing,
                                 fontFamily, errors);
    }

    // ─── Get AEM Component name by walking up the DOM ─────────────────────────
    private String getAEMComponent(ElementHandle el, String directClasses) {
        // Walk up to 10 ancestors looking for data attributes or class keywords
        Object result = el.evaluate(
            "el => {\n" +
            "  var componentClassMap = " + buildComponentClassMapJS() + ";\n" +
            "  function findBestMatch(classes) {\n" +
            "    var bestMatch = null; var bestLength = 0;\n" +
            "    for (var c = 0; c < classes.length; c++) {\n" +
            "      var cls = classes[c].toLowerCase();\n" +
            "      for (var m = 0; m < componentClassMap.length; m++) {\n" +
            "        if (cls.indexOf(componentClassMap[m][0]) !== -1) {\n" +
            "          if (componentClassMap[m][0].length > bestLength) {\n" +
            "            bestLength = componentClassMap[m][0].length;\n" +
            "            bestMatch  = componentClassMap[m];\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "    return bestMatch;\n" +
            "  }\n" +
            "  var node = el; var bestName = ''; var depth = 0;\n" +
            "  while (node && node !== document.body && depth < 10) {\n" +
            "    var dataComp = node.getAttribute('data-component') ||\n" +
            "                   node.getAttribute('data-cmp-name')  ||\n" +
            "                   node.getAttribute('data-type')       ||\n" +
            "                   node.getAttribute('data-module');\n" +
            "    if (dataComp) return dataComp.replace(/[-_]/g,'  ')\n" +
            "      .replace(/\\b\\w/g, function(c){ return c.toUpperCase(); });\n" +
            "    if (node.className && typeof node.className === 'string') {\n" +
            "      var classes = node.className.trim().split(/\\s+/);\n" +
            "      var best = findBestMatch(classes);\n" +
            "      if (best && best[0].length > bestName.length)\n" +
            "        bestName = best[1];\n" +
            "    }\n" +
            "    node = node.parentElement; depth++;\n" +
            "  }\n" +
            "  if (bestName) return bestName;\n" +
            "  var p = el.parentElement;\n" +
            "  if (p && p.className && typeof p.className === 'string') {\n" +
            "    var raw = p.className.trim().split(/\\s+/)[0];\n" +
            "    if (raw) return 'Class: ' + raw;\n" +
            "  }\n" +
            "  return 'Unknown Component';\n" +
            "}"
        );

        return result != null ? result.toString() : "Unknown Component";
    }

    // ─── Build JS array literal from Java component class map ─────────────────
    private String buildComponentClassMapJS() {
        StringBuilder sb = new StringBuilder("[");
        for (String[] entry : TypographySpecs.COMPONENT_CLASS_MAP) {
            sb.append("[\"").append(entry[0])
              .append("\",\"").append(entry[1]).append("\"],");
        }
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("]");
        return sb.toString();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────
    private boolean isWithinTolerance(double actual, double expected,
                                       double tolerance) {
        return Math.abs(actual - expected) <= tolerance;
    }

    // Accept either Aeonik or Myriad as primary plus the common fallbacks
    private boolean isValidFontFamily(String fontFamily) {
        if (fontFamily == null || fontFamily.isEmpty()) return false;
        String lower = fontFamily.toLowerCase();
        boolean hasPrimary = Arrays.stream(TypographySpecs.PRIMARY_FONT_KEYWORDS)
                                   .anyMatch(lower::contains);
        boolean hasFallbacks = Arrays.stream(TypographySpecs.FALLBACK_FONT_KEYWORDS)\n                                     .allMatch(lower::contains);
        return hasPrimary && hasFallbacks;
    }

    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try { return Double.parseDouble(value.toString()); }
        catch (NumberFormatException e) { return 0.0; }
    }

    // ─── Screenshot helper ────────────────────────────────────────────────────
    public byte[] takeScreenshot() {
        return page.screenshot(new Page.ScreenshotOptions().setFullPage(false));
    }
}