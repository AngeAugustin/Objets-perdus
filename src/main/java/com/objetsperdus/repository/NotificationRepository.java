package com.objetsperdus.repository;

import com.objetsperdus.model.Notification;
import com.objetsperdus.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    List<Notification> findByUserAndReadOrderByCreatedAtDesc(User user, boolean read);
    
    long countByUserAndRead(User user, boolean read);
}