package com.anz.typography.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates all element results for one HTML tag (h1, h2, h3, h4, p).
 */
public class TagResult {

    private final String tag;
    private final String label;
    private final List<ElementResult> passed;
    private final List<ElementResult> failed;
    private final List<String>        warnings;
    private final int                 totalFound;

    public TagResult(String tag, String label,
                     List<ElementResult> passed,
                     List<ElementResult> failed,
                     List<String> warnings) {
        this.tag       = tag;
        this.label     = label;
        this.passed    = passed   != null ? passed   : new ArrayList<>();
        this.failed    = failed   != null ? failed   : new ArrayList<>();
        this.warnings  = warnings != null ? warnings : new ArrayList<>();
        this.totalFound = this.passed.size() + this.failed.size();
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String             getTag()       { return tag;        }
    public String             getLabel()     { return label;      }
    public List<ElementResult> getPassed()   { return passed;     }
    public List<ElementResult> getFailed()   { return failed;     }
    public List<String>       getWarnings()  { return warnings;   }
    public int                getTotalFound(){ return totalFound;  }

    public boolean hasFailures() { return !failed.isEmpty();  }
    public boolean hasWarnings() { return !warnings.isEmpty(); }

    public String getStatus() {
        if (hasFailures()) return "FAIL";
        if (hasWarnings()) return "WARN";
        return "PASS";
    }

    public double getPassRate() {
        if (totalFound == 0) return 0;
        return (passed.size() * 100.0) / totalFound;
    }
}