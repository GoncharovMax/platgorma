package ru.goncharov.study.platforma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.goncharov.study.platforma.Entity.CatalogItem;

import java.util.List;

public interface CatalogItemRepository extends JpaRepository<CatalogItem, Long> {

    List<CatalogItem> findByCategory(String category);
}