package com.objetsperdus.repository;

import com.objetsperdus.model.FoundItem;
import com.objetsperdus.model.ItemCategory;
import com.objetsperdus.model.ItemStatus;
import com.objetsperdus.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FoundItemRepository extends JpaRepository<FoundItem, Long> {
    
    Page<FoundItem> findByStatus(ItemStatus status, Pageable pageable);
    
    Page<FoundItem> findByUser(User user, Pageable pageable);
    
    Page<FoundItem> findByCategory(ItemCategory category, Pageable pageable);
    
    Page<FoundItem> findByStatusAndCategory(ItemStatus status, ItemCategory category, Pageable pageable);
    
    @Query("SELECT fi FROM FoundItem fi WHERE fi.status = :status AND "
            + "(LOWER(fi.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(fi.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(fi.location) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<FoundItem> searchByKeyword(@Param("status") ItemStatus status, @Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT fi FROM FoundItem fi WHERE fi.status = :status AND "
            + "fi.category = :category AND "
            + "fi.foundDate BETWEEN :startDate AND :endDate")
    Page<FoundItem> findByFilters(
            @Param("status") ItemStatus status,
            @Param("category") ItemCategory category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
    
    List<FoundItem> findTop5ByStatusOrderByCreatedAtDesc(ItemStatus status);
}