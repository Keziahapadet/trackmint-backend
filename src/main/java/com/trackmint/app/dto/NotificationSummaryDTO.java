package com.trackmint.app.dto;

import lombok.Data;
import java.util.List;

@Data
public class NotificationSummaryDTO {
    private Long unreadCount;
    private List<NotificationDTO> recentNotifications;
    private List<NotificationDTO> unreadNotifications;
}