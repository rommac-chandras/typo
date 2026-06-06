package com.anz.typography.utils;

import com.anz.typography.model.ElementResult;
import com.anz.typography.model.ElementResult.PropertyError;
import com.anz.typography.model.TagResult;
import com.anz.typography.model.ValidationReport;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Generates console-style and HTML reports matching the original JS output.
 */
public class ReportGenerator {

    private static final String REPORTS_DIR = "test-output/typography-reports";

    // ─── Console Output (mirrors the JS script's console.log format) ──────────
    public static String buildConsoleReport(ValidationReport report) {
        StringBuilder sb = new StringBuilder();

        String border = "══════════════════════════════════════════════════";
        String ts = DateTimeFormatter
            .ofPattern("dd-MMM-yyyy HH:mm:ss")
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(report.getTimestamp()));

        sb.append("\n").append(border).append("\n");
        sb.append("  ANZ TYPOGRAPHY VALIDATION — DESKTOP\n");
        sb.append(border).append("\n");
        sb.append("  Page  : ").append(report.getPageUrl()).append("\n");
        sb.append("  Title : ").append(report.getPageTitle()).append("\n");
        sb.append("  Date  : ").append(ts).append("\n\n");

        // ── Spec table header ─────────────────────────────────────────────────
        sb.append("  FIGMA DESKTOP SPECIFICATIONS\n");
        sb.append(String.format("  %-12s %-10s %-12s %-14s%n",
            "Tag", "FontSize", "LineHeight", "LetterSpacing"));
        sb.append("  " + "─".repeat(50) + "\n");
        sb.append(String.format("  %-12s %-10s %-12s %-14s%n",
            "H1 Heading","48px","58px","0%"));
        sb.append(String.format("  %-12s %-10s %-12s %-14s%n",
            "H2 Heading","32px","42px","0.5%"));
        sb.append(String.format("  %-12s %-10s %-12s %-14s%n",
            "H3 Heading","24px","36px","1.5%"));
        sb.append(String.format("  %-12s %-10s %-12s %-14s%n",
            "H4 Body","16px","24px","2%"));
        sb.append(String.format("  %-12s %-10s %-12s %-14s%n",
            "Body Small","14px","21px","2%"));
        sb.append("\n");

        // ── Overall summary ───────────────────────────────────────────────────
        sb.append(border).append("\n");
        sb.append("  OVERALL SUMMARY\n");
        sb.append(border).append("\n");
        sb.append("  ✅ Total Passed   : ").append(report.getTotalPassed()).append("\n");
        sb.append("  ❌ Total Failed   : ").append(report.getTotalFailed()).append("\n");
        sb.append("  ⚠️  Total Warnings : ").append(report.getTotalWarnings()).append("\n");
        sb.append(String.format("  📊 Pass Rate      : %.1f%%\n",
                                report.getOverallPassRate()));
        sb.append("\n");

        // ── Per-tag detail ────────────────────────────────────────────────────
        for (Map.Entry<String, TagResult> entry : report.getTagResults().entrySet()) {
            String    tag = entry.getKey();
            TagResult r   = entry.getValue();

            String statusIcon = r.hasFailures() ? "❌ FAIL"
                              : r.hasWarnings() ? "⚠️  WARN"
                              : "✅ PASS";

            sb.append("\n┌──────────────────────────────────────────────────────\n");
            sb.append("│  ").append(statusIcon).append("  |  ")
              .append(tag.toUpperCase()).append(" — ").append(r.getLabel())
              .append("  |  Found: ").append(r.getTotalFound()).append("\n");
            sb.append("│  ✅ Passed: ").append(r.getPassed().size())
              .append("   ❌ Failed: ").append(r.getFailed().size()).append("\n");
            sb.append("└──────────────────────────────────────────────────────\n");

            // Warnings
            for (String w : r.getWarnings()) {
                sb.append("  ⚠️  ").append(w).append("\n");
            }

            // Failed elements detail
            if (!r.getFailed().isEmpty()) {
                sb.append("  ── FAILED ELEMENTS ──────────────────────────────\n");
                for (ElementResult fi : r.getFailed()) {
                    sb.append("  ❌ [").append(tag.toUpperCase()).append(" #")
                      .append(fi.getIndex()).append("]  Component → ")
                      .append(fi.getAemComponent()).append("\n");
                    sb.append("     Text: \"").append(fi.getTextPreview())
                      .append("\"\n");
                    for (PropertyError err : fi.getErrors()) {
                        sb.append("     ").append(err.toString()).append("\n");
                    }
                }
            }

            // Passed elements summary
            if (!r.getPassed().isEmpty()) {
                sb.append("  ── PASSED ELEMENTS ──────────────────────────────\n");
                for (ElementResult pi : r.getPassed()) {
                    sb.append(String.format(
                        "  ✅ [%s #%d] %-25s fontSize=%.1fpx "
                        + "lineHeight=%.1fpx letterSpacing=%.2fpx%n",
                        tag.toUpperCase(), pi.getIndex(),
                        pi.getAemComponent(),
                        pi.getActualFontSize(),
                        pi.getActualLineHeight(),
                        pi.getActualLetterSpacing()
                    ));
                }
            }
        }

        sb.append("\n").append(border).append("\n");
        if (report.isFullyPassed()) {
            sb.append("  🎉 ALL CHECKS PASSED!\n");
        } else {
            sb.append("  ❌ ").append(report.getTotalFailed())
              .append(" ISSUE(S) FOUND\n");
        }
        sb.append(border).append("\n");

        return sb.toString();
    }

    // ─── HTML Report ──────────────────────────────────────────────────────────
    public static void writeHtmlReport(ValidationReport report,
                                       String safeFileName) {
        try {
            Path dir = Paths.get(REPORTS_DIR);
            Files.createDirectories(dir);

            Path file = dir.resolve(safeFileName + ".html");

            try (PrintWriter pw = new PrintWriter(new FileWriter(file.toFile()))) {
                pw.println(buildHtmlReport(report));
            }

            System.out.println("  📄 HTML report saved: " + file.toAbsolutePath());

        } catch (IOException e) {
            System.err.println("  ⚠️  Could not write HTML report: " + e.getMessage());
        }
    }

    private static String buildHtmlReport(ValidationReport report) {
        String ts = DateTimeFormatter
            .ofPattern("dd-MMM-yyyy HH:mm:ss")
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(report.getTimestamp()));

        String passColor  = report.isFullyPassed() ? "#00aa00" : "#cc0000";
        String passIcon   = report.isFullyPassed() ? "🎉 ALL PASSED" : "❌ ISSUES FOUND";
        String passRate   = String.format("%.1f", report.getOverallPassRate());

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='en'><head>")
          .append("<meta charset='UTF-8'>")
          .append("<meta name='viewport' content='width=device-width,initial-scale=1'>")
          .append("<title>ANZ Typography Report — ")
          .append(escHtml(report.getPageTitle())).append("</title>")
          .append(getCss())
          .append("</head><body>");

        // Header banner
        sb.append("<div class='header'>")
          .append("<h1>🔍 ANZ Typography Validator</h1>")
          .append("<div class='meta'>")
          .append("<span>📄 ").append(escHtml(report.getPageTitle())).append("</span>")
          .append("<span>🔗 <a href='").append(escHtml(report.getPageUrl()))
          .append("' target='_blank'>").append(escHtml(report.getPageUrl()))
          .append("</a></span>")
          .append("<span>🕐 ").append(ts).append("</span>")
          .append("</div></div>");

        // Summary cards
        sb.append("<div class='summary'>")
          .append(summaryCard("✅ Passed",  String.valueOf(report.getTotalPassed()),  "pass"))
          .append(summaryCard("❌ Failed",  String.valueOf(report.getTotalFailed()),  "fail"))
          .append(summaryCard("⚠️ Warnings",String.valueOf(report.getTotalWarnings()),"warn"))
          .append(summaryCard("📊 Pass Rate", passRate + "%",
                              report.isFullyPassed() ? "pass" : "fail"))
          .append("</div>");

        // Overall status banner
        sb.append("<div class='status-banner' style='background:")
          .append(passColor).append("'>").append(passIcon).append("</div>");

        // Per-tag sections
        for (Map.Entry<String, TagResult> entry : report.getTagResults().entrySet()) {
            String    tag = entry.getKey();
            TagResult r   = entry.getValue();

            String sectionClass = r.hasFailures() ? "fail"
                                : r.hasWarnings() ? "warn"
                                : "pass";
            String statusIcon   = r.hasFailures() ? "❌"
                                : r.hasWarnings() ? "⚠️"
                                : "✅";

            sb.append("<div class='tag-section ").append(sectionClass).append("'>")
              .append("<div class='tag-header'>")
              .append(statusIcon).append(" ").append(tag.toUpperCase())
              .append(" — ").append(escHtml(r.getLabel()))
              .append(" &nbsp;|&nbsp; Found: ").append(r.getTotalFound())
              .append(" &nbsp;|&nbsp; ✅ ").append(r.getPassed().size())
              .append(" &nbsp; ❌ ").append(r.getFailed().size())
              .append("</div>");

            // Warnings
            for (String w : r.getWarnings()) {
                sb.append("<div class='warning'>⚠️  ").append(escHtml(w))
                  .append("</div>");
            }

            // Failed table
            if (!r.getFailed().isEmpty()) {
                sb.append("<h4>Failed Elements</h4>")
                  .append("<table><thead><tr>")
                  .append("<th>#</th><th>Component</th><th>Text Preview</th>")
                  .append("<th>Property</th><th>Expected</th><th>Actual</th>")
                  .append("</tr></thead><tbody>");

                for (ElementResult fi : r.getFailed()) {
                    boolean firstRow = true;
                    int     rowspan  = fi.getErrors().size();

                    for (PropertyError err : fi.getErrors()) {
                        sb.append("<tr class='fail-row'>");
                        if (firstRow) {
                            sb.append("<td rowspan='").append(rowspan).append("'>")
                              .append(fi.getIndex()).append("</td>")
                              .append("<td rowspan='").append(rowspan).append("'>")
                              .append(escHtml(fi.getAemComponent())).append("</td>")
                              .append("<td rowspan='").append(rowspan).append("'>")
                              .append(escHtml(fi.getTextPreview())).append("</td>");
                            firstRow = false;
                        }
                        sb.append("<td>").append(escHtml(err.property)).append("</td>")
                          .append("<td class='expected'>")
                          .append(escHtml(err.expected)).append("</td>")
                          .append("<td class='actual'>")
                          .append(escHtml(err.actual)).append("</td>")
                          .append("</tr>");
                    }
                }
                sb.append("</tbody></table>");
            }

            // Passed table
            if (!r.getPassed().isEmpty()) {
                sb.append("<h4>Passed Elements</h4>")
                  .append("<table><thead><tr>")
                  .append("<th>#</th><th>Component</th><th>Text Preview</th>")
                  .append("<th>FontSize</th><th>LineHeight</th>")
                  .append("<th>LetterSpacing</th>")
                  .append("</tr></thead><tbody>");

                for (ElementResult pi : r.getPassed()) {
                    sb.append("<tr class='pass-row'>")
                      .append("<td>").append(pi.getIndex()).append("</td>")
                      .append("<td>").append(escHtml(pi.getAemComponent())).append("</td>")
                      .append("<td>").append(escHtml(pi.getTextPreview())).append("</td>")
                      .append("<td>").append(pi.getActualFontSize()).append("px</td>")
                      .append("<td>").append(pi.getActualLineHeight()).append("px</td>")
                      .append("<td>")
                      .append(String.format("%.2f", pi.getActualLetterSpacing()))
                      .append("px</td>")
                      .append("</tr>");
                }
                sb.append("</tbody></table>");
            }

            sb.append("</div>"); // end tag-section
        }

        sb.append("<div class='footer'>")
          .append("ANZ Typography Validator — Generated ").append(ts)
          .append("</div>");

        sb.append("</body></html>");
        return sb.toString();
    }

    // ─── CSS for HTML report ──────────────────────────────────────────────────
    private static String getCss() {
        return "<style>" +
            "body{font-family:Arial,sans-serif;margin:0;padding:0;" +
                 "background:#f5f5f5;color:#333}" +
            ".header{background:#1a1a2e;color:#fff;padding:20px 30px}" +
            ".header h1{margin:0 0 8px 0;color:#4da6ff;font-size:22px}" +
            ".meta{display:flex;gap:20px;flex-wrap:wrap;font-size:13px;color:#aaa}" +
            ".meta a{color:#4da6ff}" +
            ".summary{display:flex;gap:16px;padding:20px 30px;flex-wrap:wrap}" +
            ".card{background:#fff;border-radius:8px;padding:16px 24px;" +
                  "box-shadow:0 2px 8px rgba(0,0,0,0.1);min-width:120px;text-align:center}" +
            ".card .value{font-size:28px;font-weight:bold}" +
            ".card .label{font-size:12px;color:#888;margin-top:4px}" +
            ".card.pass .value{color:#00aa00}" +
            ".card.fail .value{color:#cc0000}" +
            ".card.warn .value{color:#cc7700}" +
            ".status-banner{color:#fff;text-align:center;padding:12px;" +
                           "font-size:18px;font-weight:bold;margin:0 30px 20px}" +
            ".tag-section{background:#fff;margin:0 30px 20px;border-radius:8px;" +
                         "box-shadow:0 2px 8px rgba(0,0,0,0.1);overflow:hidden}" +
            ".tag-section.fail .tag-header{background:#cc0000;color:#fff}" +
            ".tag-section.pass .tag-header{background:#007700;color:#fff}" +
            ".tag-section.warn .tag-header{background:#cc7700;color:#fff}" +
            ".tag-header{padding:12px 20px;font-weight:bold;font-size:14px}" +
            "h4{margin:16px 20px 8px;color:#444;font-size:13px;text-transform:uppercase}" +
            "table{width:calc(100% - 40px);margin:0 20px 16px;border-collapse:collapse;" +
                  "font-size:12px}" +
            "th{background:#f0f0f0;padding:8px;text-align:left;border:1px solid #ddd;" +
               "font-weight:bold}" +
            "td{padding:7px 8px;border:1px solid #eee;vertical-align:top}" +
            ".fail-row td{background:#fff8f8}" +
            ".pass-row td{background:#f8fff8}" +
            ".expected{color:#007700;font-weight:bold}" +
            ".actual{color:#cc0000;font-weight:bold}" +
            ".warning{background:#fff3cd;padding:8px 20px;color:#856404;font-size:13px}" +
            ".footer{text-align:center;padding:20px;color:#888;font-size:11px}" +
            "</style>";
    }

    private static String summaryCard(String label, String value, String cls) {
        return "<div class='card " + cls + "'>" +
               "<div class='value'>" + value + "</div>" +
               "<div class='label'>" + label + "</div>" +
               "</div>";
    }

    private static String escHtml(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
                .replace("\"","&quot;").replace("'","&#39;");
    }
}