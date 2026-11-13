package com.campusflow.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a college or club event (including flagship event Lakshya).
 */
public class Event {

    public static final String STATUS_SCHEDULE_PENDING = "SCHEDULE_PENDING";
    public static final String STATUS_PENDING_APPROVAL = "PENDING_APPROVAL";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private int eventId;
    private String eventCode;
    private boolean flagship;
    private String eventName;
    private String description;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String venue;
    private String organizer;
    private LocalDateTime registrationDeadline;
    private Integer capacity;
    private String status;
    private Integer createdBy;
    private String createdByName;
    private LocalDateTime createdAt;

    private int registeredCount;
    private int presentCount;
    private boolean userRegistered;
    private List<Club> associatedClubs = new ArrayList<>();

    public Event() {
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public boolean isFlagship() {
        return flagship;
    }

    public void setFlagship(boolean flagship) {
        this.flagship = flagship;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public LocalDateTime getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(LocalDateTime registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getRegisteredCount() {
        return registeredCount;
    }

    public void setRegisteredCount(int registeredCount) {
        this.registeredCount = registeredCount;
    }

    public int getPresentCount() {
        return presentCount;
    }

    public void setPresentCount(int presentCount) {
        this.presentCount = presentCount;
    }

    public boolean isUserRegistered() {
        return userRegistered;
    }

    public void setUserRegistered(boolean userRegistered) {
        this.userRegistered = userRegistered;
    }

    public List<Club> getAssociatedClubs() {
        return associatedClubs;
    }

    public void setAssociatedClubs(List<Club> associatedClubs) {
        this.associatedClubs = (associatedClubs == null) ? new ArrayList<>() : associatedClubs;
    }

    public boolean hasClub(int clubId) {
        for (Club c : associatedClubs) {
            if (c.getClubId() == clubId) {
                return true;
            }
        }
        return false;
    }

    public Integer getRemainingSeats() {
        if (capacity == null) {
            return null;
        }
        return Math.max(0, capacity - registeredCount);
    }

    public boolean isFull() {
        return capacity != null && registeredCount >= capacity;
    }

    public boolean isDeadlinePassed(LocalDateTime now) {
        return registrationDeadline != null && now.isAfter(registrationDeadline);
    }

    public boolean isRegistrationOpen(LocalDate today, LocalDateTime now) {
        if (!STATUS_APPROVED.equals(status)) {
            return false;
        }
        if (eventDate == null || eventDate.isBefore(today)) {
            return false;
        }
        if (isDeadlinePassed(now)) {
            return false;
        }
        return !isFull();
    }

    public String getStatusDisplayName() {
        if (status == null) {
            return "Unknown";
        }
        return switch (status) {
            case STATUS_SCHEDULE_PENDING -> "Schedule Pending";
            case STATUS_PENDING_APPROVAL -> "Pending Approval";
            case STATUS_APPROVED -> "Approved / Open";
            case STATUS_REJECTED -> "Rejected";
            case STATUS_COMPLETED -> "Completed";
            case STATUS_CANCELLED -> "Cancelled";
            default -> status;
        };
    }

    public String getStatusBadgeClass() {
        if (status == null) {
            return "badge-soft-secondary";
        }
        return switch (status) {
            case STATUS_APPROVED -> "badge-soft-success";
            case STATUS_PENDING_APPROVAL, STATUS_SCHEDULE_PENDING -> "badge-soft-warning";
            case STATUS_REJECTED, STATUS_CANCELLED -> "badge-soft-danger";
            case STATUS_COMPLETED -> "badge-soft-info";
            default -> "badge-soft-secondary";
        };
    }

    public String getStatusIconClass() {
        if (status == null) {
            return "bi-info-circle";
        }
        return switch (status) {
            case STATUS_APPROVED -> "bi-check-circle-fill";
            case STATUS_PENDING_APPROVAL -> "bi-hourglass-split";
            case STATUS_SCHEDULE_PENDING -> "bi-calendar-event";
            case STATUS_REJECTED -> "bi-x-octagon-fill";
            case STATUS_CANCELLED -> "bi-slash-circle-fill";
            case STATUS_COMPLETED -> "bi-award-fill";
            default -> "bi-info-circle";
        };
    }
}
