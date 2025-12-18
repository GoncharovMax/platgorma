package ru.goncharov.study.platforma.Entity;

public enum CatalogCategory {
    QUARTZ_LAMINATE("Кварцевый ламинат"),
    TILE("Плитка"),
    WALLPAPER("Обои");

    private final String title;

    CatalogCategory(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}