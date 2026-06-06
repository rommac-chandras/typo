package com.anz.typography.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Top-level container for a full page validation report.
 */
public class ValidationReport {

    private final String                  pageUrl;
    private final String                  pageTitle;
    private final Map<String, TagResult>  tagResults;
    private final long                    timestamp;

    private int totalPassed   = 0;
    private int totalFailed   = 0;
    private int totalWarnings = 0;

    public ValidationReport(String pageUrl, String pageTitle) {
        this.pageUrl    = pageUrl;
        this.pageTitle  = pageTitle;
        this.tagResults = new LinkedHashMap<>();
        this.timestamp  = System.currentTimeMillis();
    }

    public void addTagResult(String tag, TagResult result) {
        tagResults.put(tag, result);
        totalPassed   += result.getPassed().size();
        totalFailed   += result.getFailed().size();
        totalWarnings += result.getWarnings().size();
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String                  getPageUrl()      { return pageUrl;      }
    public String                  getPageTitle()    { return pageTitle;    }
    public Map<String, TagResult>  getTagResults()   { return tagResults;   }
    public long                    getTimestamp()    { return timestamp;    }
    public int                     getTotalPassed()  { return totalPassed;  }
    public int                     getTotalFailed()  { return totalFailed;  }
    public int                     getTotalWarnings(){ return totalWarnings; }

    public boolean isFullyPassed() {
        return totalFailed == 0;
    }

    public double getOverallPassRate() {
        int total = totalPassed + totalFailed;
        if (total == 0) return 0;
        return (totalPassed * 100.0) / total;
    }
}