package com.trackmint.app.service;

import com.trackmint.app.dto.NotificationDTO;
import com.trackmint.app.dto.NotificationRequestDTO;
import com.trackmint.app.dto.NotificationSummaryDTO;
import com.trackmint.app.entity.Notification;
import com.trackmint.app.entity.User;
import com.trackmint.app.repository.NotificationRepository;
import com.trackmint.app.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }



    @Transactional(readOnly = true)
    public List<NotificationDTO> getUserNotifications(String email) {
        User user = findUserByEmail(email);
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotifications(String email) {
        User user = findUserByEmail(email);
        return notificationRepository
                .findByUserAndIsReadFalseOrderByCreatedAtDesc(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount(String email) {
        User user = findUserByEmail(email);
        return notificationRepository.countUnreadByUser(user);
    }

    @Transactional(readOnly = true)
    public NotificationSummaryDTO getNotificationSummary(String email) {
        User user = findUserByEmail(email);
        NotificationSummaryDTO summary = new NotificationSummaryDTO();


        List<NotificationDTO> unread = notificationRepository
                .findByUserAndIsReadFalseOrderByCreatedAtDesc(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        summary.setUnreadCount((long) unread.size()); // count from list
        summary.setUnreadNotifications(unread);        // set the list

        List<NotificationDTO> recent = notificationRepository
                .findTop10ByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        summary.setRecentNotifications(recent);

        return summary;
    }



    @Transactional
    public NotificationDTO createNotification(String email, NotificationRequestDTO dto) {
        User user = findUserByEmail(email);

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(dto.getTitle());
        notification.setMessage(dto.getMessage());
        notification.setType(dto.getType());
        notification.setLink(dto.getLink());
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);
        return convertToDTO(saved);
    }

    @Transactional
    public NotificationDTO markAsRead(String email, Long id) {
        User user = findUserByEmail(email);
        Notification notification = findNotificationById(id);
        checkOwnership(notification, user);
        notification.setRead(true);
        return convertToDTO(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllAsRead(String email) {
        User user = findUserByEmail(email);
        notificationRepository.markAllAsRead(user);
    }

    @Transactional
    public void deleteNotification(String email, Long id) {
        User user = findUserByEmail(email);
        Notification notification = findNotificationById(id);
        checkOwnership(notification, user);
        notificationRepository.delete(notification);
    }

    @Transactional
    public void deleteOldNotifications(String email) {
        User user = findUserByEmail(email);
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        notificationRepository.deleteOldReadNotifications(user, thirtyDaysAgo);
    }



    public void createBudgetAlert(String email, String category,
                                  double percentage, double spent, double budget) {
        if (percentage < 80) return;

        String title = "Budget Alert: " + category;
        String type;
        String message;

        if (percentage >= 100) {
            type = "BUDGET_EXCEEDED";
            message = String.format("You have exceeded your %s budget by ksh %.2f",
                    category, (spent - budget));
        } else {
            type = "BUDGET_WARNING";
            message = String.format("You have used %.0f%% of your %s budget",
                    percentage, category);
        }

        NotificationRequestDTO dto = new NotificationRequestDTO();
        dto.setTitle(title);
        dto.setMessage(message);
        dto.setType(type);
        dto.setLink("/budgets");
        createNotification(email, dto);
    }

    public void createLargeTransactionAlert(String email, String description,
                                            double amount, String type) {
        String title = "Large " + (type.equals("INCOME") ? "Income" : "Expense");
        String message = String.format("%s of ksh %.2f: %s",
                type.equals("INCOME") ? "Received" : "Spent",
                Math.abs(amount), description);

        NotificationRequestDTO dto = new NotificationRequestDTO();
        dto.setTitle(title);
        dto.setMessage(message);
        dto.setType("LARGE_TRANSACTION");
        dto.setLink("/transactions");
        createNotification(email, dto);
    }

    public void createWeeklySummary(String email, double spent,
                                    double earned, double saved) {
        String message = String.format(
                "This week: ksh %.2f earned, ksh %.2f spent, ksh %.2f saved",
                earned, spent, saved);

        NotificationRequestDTO dto = new NotificationRequestDTO();
        dto.setTitle("Weekly Summary");
        dto.setMessage(message);
        dto.setType("WEEKLY_SUMMARY");
        dto.setLink("/dashboard");
        createNotification(email, dto);
    }



    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Notification findNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
    }

    private void checkOwnership(Notification notification, User user) {
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized");
        }
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setLink(notification.getLink());
        dto.setTimeAgo(calculateTimeAgo(notification.getCreatedAt()));
        return dto;
    }

    private String calculateTimeAgo(LocalDateTime dateTime) {

        LocalDateTime now = LocalDateTime.now(java.time.ZoneOffset.UTC);
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long days = ChronoUnit.DAYS.between(dateTime, now);

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " min ago";
        if (hours < 24) return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        if (days < 7) return days + " day" + (days > 1 ? "s" : "") + " ago";
        return dateTime.toLocalDate().toString();
    }
}