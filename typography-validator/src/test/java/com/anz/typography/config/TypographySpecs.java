package com.anz.typography.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Desktop typography specifications matching Figma design system.
 * These specs define expected CSS values for each heading/text tag.
 */
public class TypographySpecs {

    // ── Inner class representing one tag's specification ──────────────────────
    public static class Spec {
        public final double fontSize;       // px
        public final double lineHeight;     // px
        public final double letterSpacing;  // % (e.g. 0.5 = 0.5%)
        public final String label;

        public Spec(double fontSize, double lineHeight,
                    double letterSpacing, String label) {
            this.fontSize      = fontSize;
            this.lineHeight    = lineHeight;
            this.letterSpacing = letterSpacing;
            this.label         = label;
        }

        /**
         * Converts letterSpacing % to px:
         * letterSpacingPx = (letterSpacing / 100) * fontSize
         */
        public double expectedLetterSpacingPx() {
            return (letterSpacing / 100.0) * fontSize;
        }
    }

    // ── Desktop Specs Map — keyed by CSS selector ─────────────────────────────
    public static final Map<String, Spec> DESKTOP_SPECS = new LinkedHashMap<>();

    static {
        DESKTOP_SPECS.put("h1", new Spec(48, 58, 0,   "H1 Heading"));
        DESKTOP_SPECS.put("h2", new Spec(32, 42, 0.5, "H2 Heading"));
        DESKTOP_SPECS.put("h3", new Spec(24, 36, 1.5, "H3 Heading"));
        DESKTOP_SPECS.put("h4", new Spec(16, 24, 2,   "H4 Body"));
        DESKTOP_SPECS.put("p",  new Spec(14, 21, 2,   "Body Small"));
    }

    // ── Tolerance values ──────────────────────────────────────────────────────
    public static final double FONT_SIZE_TOLERANCE      = 1.0;
    public static final double LINE_HEIGHT_TOLERANCE    = 2.0;
    public static final double LETTER_SPACING_TOLERANCE = 0.5;

    // ── Expected font family keywords
    // Note: some pages use "myriad-pro" while others use "aeonik" as primary.
    // Validate that at least one primary is present plus common fallbacks.
    public static final String[] PRIMARY_FONT_KEYWORDS = {
        "aeonik", "myriad-pro"
    };

    public static final String[] FALLBACK_FONT_KEYWORDS = {
        "arial", "helvetica", "sans-serif"
    };

    // ── ANZ Component class keyword mapping ───────────────────────────────────
    public static final String[][] COMPONENT_CLASS_MAP = {
        {"footer__tertiarytext",  "Footer - Tertiary Text"},
        {"footer__tertiary",      "Footer - Tertiary Text"},
        {"footer__secondarytext", "Footer - Secondary Text"},
        {"footer__secondary",     "Footer - Secondary Links"},
        {"footer__primarytext",   "Footer - Primary Text"},
        {"footer__primary",       "Footer - Primary Links"},
        {"footer__bottom",        "Footer - Bottom Bar"},
        {"footer__social",        "Footer - Social Links"},
        {"footer__legal",         "Footer - Legal Text"},
        {"footer__logo",          "Footer - Logo"},
        {"footer",                "Footer"},
        {"header",                "Header"},
        {"nav",                   "Navigation"},
        {"navigation",            "Navigation"},
        {"mega-menu",             "Mega Menu"},
        {"main-nav",              "Main Navigation"},
        {"hero",                  "Hero Banner"},
        {"banner",                "Banner"},
        {"card__title",           "Card - Title"},
        {"card__body",            "Card - Body"},
        {"card__description",     "Card - Description"},
        {"card__cta",             "Card - CTA"},
        {"card",                  "Card"},
        {"product-tile__title",   "Product Tile - Title"},
        {"product-tile__desc",    "Product Tile - Desc"},
        {"product-tile",          "Product Tile"},
        {"promo__title",          "Promo - Title"},
        {"promo__body",           "Promo - Body"},
        {"promo",                 "Promo"},
        {"accordion__title",      "Accordion - Title"},
        {"accordion__body",       "Accordion - Body"},
        {"accordion",             "Accordion"},
        {"tab__title",            "Tab - Title"},
        {"tab__content",          "Tab - Content"},
        {"tabs",                  "Tabs"},
        {"modal__title",          "Modal - Title"},
        {"modal__body",           "Modal - Body"},
        {"modal",                 "Modal"},
        {"form__label",           "Form - Label"},
        {"form__error",           "Form - Error Message"},
        {"form__helper",          "Form - Helper Text"},
        {"form",                  "Form"},
        {"cmp-title",             "AEM Title Component"},
        {"cmp-text",              "AEM Text Component"},
        {"cmp-teaser",            "AEM Teaser"},
        {"cmp-carousel",          "AEM Carousel"},
        {"cmp-accordion",         "AEM Accordion"},
        {"cmp-tabs",              "AEM Tabs"},
        {"cmp-navigation",        "AEM Navigation"},
        {"cmp-breadcrumb",        "AEM Breadcrumb"},
        {"cmp",                   "AEM Component"},
        {"anz-component",         "ANZ Component"},
        {"section__title",        "Section - Title"},
        {"section__body",         "Section - Body"},
        {"section",               "Section"},
        {"richtext",              "Rich Text"},
        {"content",               "Content Area"},
        {"sidebar",               "Sidebar"},
        {"main",                  "Main Content"}
    };
}