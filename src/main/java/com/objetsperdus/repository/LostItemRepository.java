package com.objetsperdus.repository;

import com.objetsperdus.model.ItemCategory;
import com.objetsperdus.model.ItemStatus;
import com.objetsperdus.model.LostItem;
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
public interface LostItemRepository extends JpaRepository<LostItem, Long> {
    
    Page<LostItem> findByStatus(ItemStatus status, Pageable pageable);
    
    Page<LostItem> findByUser(User user, Pageable pageable);
    
    Page<LostItem> findByCategory(ItemCategory category, Pageable pageable);
    
    Page<LostItem> findByStatusAndCategory(ItemStatus status, ItemCategory category, Pageable pageable);
    
    @Query("SELECT li FROM LostItem li WHERE li.status = :status AND "
            + "(LOWER(li.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(li.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(li.location) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<LostItem> searchByKeyword(@Param("status") ItemStatus status, @Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT li FROM LostItem li WHERE li.status = :status AND "
            + "li.category = :category AND "
            + "li.lostDate BETWEEN :startDate AND :endDate")
    Page<LostItem> findByFilters(
            @Param("status") ItemStatus status,
            @Param("category") ItemCategory category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
    
    List<LostItem> findTop5ByStatusOrderByCreatedAtDesc(ItemStatus status);
}