package com.dwestermann.erp.product.domain;

import lombok.Getter;

@Getter
public enum ProductStatus {
    DRAFT("Entwurf", "Produkt ist noch in Bearbeitung", "#FFA500"),
    ACTIVE("Aktiv", "Produkt ist verfügbar und verkaufbar", "#28A745"),
    DISCONTINUED("Eingestellt", "Produkt wird nicht mehr verkauft", "#DC3545"),
    OUT_OF_STOCK("Nicht verfügbar", "Produkt ist vorübergehend nicht verfügbar", "#6C757D");

    private final String displayName;
    private final String description;
    private final String color; // Hex color for UI

    ProductStatus(String displayName, String description, String color) {
        this.displayName = displayName;
        this.description = description;
        this.color = color;
    }

    public boolean isAvailableForSale() {
        return this == ACTIVE;
    }

    public boolean allowsStockChanges() {
        return this == ACTIVE || this == OUT_OF_STOCK;
    }
}