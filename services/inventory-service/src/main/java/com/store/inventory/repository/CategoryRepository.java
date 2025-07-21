package com.store.inventory.repository;

import com.store.inventory.model.Category;
import com.store.inventory.model.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Category entity operations.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find a category by its name and tenant ID.
     */
    Optional<Category> findByNameAndTenantId(String name, Long tenantId);
    
    /**
     * Find all active categories for a tenant.
     */
    List<Category> findAllByTenantIdAndStatus(Long tenantId, EntityStatus status);
    
    /**
     * Find all top-level categories (categories without a parent) for a tenant.
     */
    List<Category> findAllByTenantIdAndParentIsNull(Long tenantId);
    
    /**
     * Find all active top-level categories for a tenant.
     */
    List<Category> findAllByTenantIdAndParentIsNullAndStatus(Long tenantId, EntityStatus status);
    
    /**
     * Find all subcategories of a given parent category.
     */
    List<Category> findAllByParentId(Long parentId);
    
    /**
     * Find all categories for a tenant with pagination.
     */
    Page<Category> findAllByTenantId(Long tenantId, Pageable pageable);
    
    /**
     * Check if a category with the given name exists for a tenant.
     */
    boolean existsByNameAndTenantId(String name, Long tenantId);
    
    /**
     * Get the full path of a category (e.g., "Electronics > Computers > Laptops")
     */
    @Query(value = "WITH RECURSIVE category_path AS (" +
            "  SELECT id, name, parent_id, CAST(name AS VARCHAR) AS path" +
            "  FROM categories" +
            "  WHERE id = :categoryId" +
            "  UNION ALL" +
            "  SELECT c.id, c.name, c.parent_id, CAST(c.name || ' > ' || cp.path AS VARCHAR)" +
            "  FROM categories c" +
            "  JOIN category_path cp ON c.id = cp.parent_id" +
            ")" +
            "SELECT path FROM category_path WHERE parent_id IS NULL", nativeQuery = true)
    String getCategoryPath(@Param("categoryId") Long categoryId);
}
