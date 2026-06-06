package com.anz.typography.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the validation result for a single DOM element.
 */
public class ElementResult {

    public enum Status { PASS, FAIL }

    private final int    index;
    private final String aemComponent;
    private final String textPreview;
    private final Status status;

    // Actual computed values
    private final double actualFontSize;
    private final double actualLineHeight;
    private final double actualLetterSpacing;
    private final String actualFontFamily;

    // Individual property errors for FAIL cases
    private final List<PropertyError> errors;

    // ── Constructor ───────────────────────────────────────────────────────────
    public ElementResult(int index, String aemComponent, String textPreview,
                         double actualFontSize, double actualLineHeight,
                         double actualLetterSpacing, String actualFontFamily,
                         List<PropertyError> errors) {
        this.index              = index;
        this.aemComponent       = aemComponent;
        this.textPreview        = textPreview;
        this.actualFontSize     = actualFontSize;
        this.actualLineHeight   = actualLineHeight;
        this.actualLetterSpacing = actualLetterSpacing;
        this.actualFontFamily   = actualFontFamily;
        this.errors             = errors != null ? errors : new ArrayList<>();
        this.status             = this.errors.isEmpty() ? Status.PASS : Status.FAIL;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public int    getIndex()               { return index;               }
    public String getAemComponent()        { return aemComponent;        }
    public String getTextPreview()         { return textPreview;         }
    public Status getStatus()              { return status;              }
    public double getActualFontSize()      { return actualFontSize;      }
    public double getActualLineHeight()    { return actualLineHeight;    }
    public double getActualLetterSpacing() { return actualLetterSpacing; }
    public String getActualFontFamily()    { return actualFontFamily;    }
    public List<PropertyError> getErrors() { return errors;              }
    public boolean isPassed()              { return status == Status.PASS; }
    public boolean isFailed()              { return status == Status.FAIL; }

    // ── Inner class for individual property errors ─────────────────────────────
    public static class PropertyError {
        public final String property;
        public final String expected;
        public final String actual;

        public PropertyError(String property, String expected, String actual) {
            this.property = property;
            this.expected = expected;
            this.actual   = actual;
        }

        @Override
        public String toString() {
            return String.format("  %s → Expected: %s | Actual: %s",
                                 property, expected, actual);
        }
    }
}