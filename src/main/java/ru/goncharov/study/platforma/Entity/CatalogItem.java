package ru.goncharov.study.platforma.Entity;

import java.util.List;

public class CatalogItem {
    private final String name;
    private final List<String> subItems;

    public CatalogItem(String name, List<String> subItems) {
        this.name = name;
        this.subItems = subItems;
    }

    public String getName() {
        return name;
    }

    public List<String> getSubItems() {
        return subItems;
    }
}