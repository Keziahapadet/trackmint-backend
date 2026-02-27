package com.trackmint.app.controller;

import com.trackmint.app.dto.NotificationDTO;
import com.trackmint.app.dto.NotificationRequestDTO;
import com.trackmint.app.dto.NotificationSummaryDTO;
import com.trackmint.app.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification API", description = "Manage user notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Get all notifications")
    public ResponseEntity<List<NotificationDTO>> getAllNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userDetails.getUsername()));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userDetails.getUsername()));
    }

    @GetMapping("/count")
    @Operation(summary = "Get unread count")
    public ResponseEntity<Long> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userDetails.getUsername()));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get notification summary")
    public ResponseEntity<NotificationSummaryDTO> getNotificationSummary(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(notificationService.getNotificationSummary(userDetails.getUsername()));
    }

    @PostMapping
    @Operation(summary = "Create a notification (admin/system)")
    public ResponseEntity<NotificationDTO> createNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody NotificationRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.createNotification(userDetails.getUsername(), dto));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<NotificationDTO> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(userDetails.getUsername(), id));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notification")
    public ResponseEntity<Void> deleteNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        notificationService.deleteNotification(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/old")
    @Operation(summary = "Delete old read notifications")
    public ResponseEntity<Void> deleteOldNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.deleteOldNotifications(userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}