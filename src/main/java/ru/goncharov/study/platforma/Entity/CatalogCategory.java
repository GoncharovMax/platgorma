package ru.goncharov.study.platforma.Entity;

public enum CatalogCategory {

    INTERIOR("Интерьер и отделка"),
    FLOORING("Напольные покрытия"),
    MATERIALS("Стройматериалы");

    private final String displayName;

    CatalogCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}