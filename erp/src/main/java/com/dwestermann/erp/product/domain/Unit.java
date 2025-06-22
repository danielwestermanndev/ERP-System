package com.dwestermann.erp.product.domain;

import lombok.Getter;

@Getter
public enum Unit {
    // Stück-basierte Einheiten
    PIECE("Stück", "Stk", false, UnitCategory.COUNT),
    PACKAGE("Paket", "Pkg", false, UnitCategory.COUNT),
    BOX("Karton", "Ktn", false, UnitCategory.COUNT),
    PALLET("Palette", "Pal", false, UnitCategory.COUNT),
    DOZEN("Dutzend", "Dtz", false, UnitCategory.COUNT),

    // Gewichts-Einheiten
    KILOGRAM("Kilogramm", "kg", true, UnitCategory.WEIGHT),
    GRAM("Gramm", "g", true, UnitCategory.WEIGHT),
    TON("Tonne", "t", true, UnitCategory.WEIGHT),
    POUND("Pfund", "lb", true, UnitCategory.WEIGHT),

    // Volumen-Einheiten
    LITER("Liter", "l", true, UnitCategory.VOLUME),
    MILLILITER("Milliliter", "ml", true, UnitCategory.VOLUME),
    CUBIC_METER("Kubikmeter", "m³", true, UnitCategory.VOLUME),
    CUBIC_CENTIMETER("Kubikzentimeter", "cm³", true, UnitCategory.VOLUME),

    // Längen-Einheiten
    METER("Meter", "m", true, UnitCategory.LENGTH),
    CENTIMETER("Zentimeter", "cm", true, UnitCategory.LENGTH),
    MILLIMETER("Millimeter", "mm", true, UnitCategory.LENGTH),
    KILOMETER("Kilometer", "km", true, UnitCategory.LENGTH),
    INCH("Zoll", "in", true, UnitCategory.LENGTH),
    FOOT("Fuß", "ft", true, UnitCategory.LENGTH),

    // Flächen-Einheiten
    SQUARE_METER("Quadratmeter", "m²", true, UnitCategory.AREA),
    SQUARE_CENTIMETER("Quadratzentimeter", "cm²", true, UnitCategory.AREA),
    SQUARE_KILOMETER("Quadratkilometer", "km²", true, UnitCategory.AREA),

    // Zeit-Einheiten
    HOUR("Stunde", "h", true, UnitCategory.TIME),
    MINUTE("Minute", "min", true, UnitCategory.TIME),
    DAY("Tag", "Tag", false, UnitCategory.TIME),
    WEEK("Woche", "Wo", false, UnitCategory.TIME),
    MONTH("Monat", "Mon", false, UnitCategory.TIME);

    private final String displayName;
    private final String symbol;
    private final boolean allowsDecimals; // ✅ Jetzt public über @Getter
    private final UnitCategory category;

    Unit(String displayName, String symbol, boolean allowsDecimals, UnitCategory category) {
        this.displayName = displayName;
        this.symbol = symbol;
        this.allowsDecimals = allowsDecimals;
        this.category = category;
    }

    public String getFullDisplayName() {
        return String.format("%s (%s)", displayName, symbol);
    }

    // ✅ Explicit getter method für bessere Klarheit
    public boolean allowsDecimals() {
        return allowsDecimals;
    }

    public enum UnitCategory {
        COUNT("Anzahl"),
        WEIGHT("Gewicht"),
        VOLUME("Volumen"),
        LENGTH("Länge"),
        AREA("Fläche"),
        TIME("Zeit");

        @Getter
        private final String displayName;

        UnitCategory(String displayName) {
            this.displayName = displayName;
        }
    }
}