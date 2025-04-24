package com.objetsperdus.service;

import com.objetsperdus.model.Notification;
import com.objetsperdus.model.User;
import com.objetsperdus.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserService userService;
    
    @Transactional
    public Notification createNotification(User user, String title, String message) {
        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .user(user)
                .build();
                
        return notificationRepository.save(notification);
    }
    
    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        return notificationRepository.findByUserOrderByCreatedAtDesc(currentUser, pageable);
    }
    
    @Transactional
    public void markNotificationAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification non trouvée"));
                
        User currentUser = userService.getCurrentUser();
        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Vous n'êtes pas autorisé à accéder à cette notification");
        }
        
        notification.setRead(true);
        notificationRepository.save(notification);
    }
    
    @Transactional
    public void markAllNotificationsAsRead() {
        User currentUser = userService.getCurrentUser();
        notificationRepository.findByUserAndReadOrderByCreatedAtDesc(currentUser, false)
                .forEach(notification -> {
                    notification.setRead(true);
                    notificationRepository.save(notification);
                });
    }
    
    @Transactional(readOnly = true)
    public long countUnreadNotifications() {
        try {
            User currentUser = userService.getCurrentUser();
            return notificationRepository.countByUserAndRead(currentUser, false);
        } catch (Exception e) {
            return 0;
        }
    }
}