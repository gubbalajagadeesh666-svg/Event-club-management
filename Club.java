package com.campusflow.model;

import java.time.LocalDateTime;

/**
 * Represents a college club (such as Prakruthi, Spoorthi, Kruthi, Saheli, or newly added clubs).
 */
public class Club {

    private int clubId;
    private String clubSlug;
    private String clubName;
    private String focusArea;
    private String description;
    private String activities;
    private String iconClass;
    private String accentTheme;
    private Integer coordinatorId;
    private String coordinatorName;
    private Integer leaderId;
    private String leaderName;
    private int memberCount;
    private int eventCount;
    private boolean userMember;
    private LocalDateTime createdAt;

    public Club() {
    }

    public int getClubId() {
        return clubId;
    }

    public void setClubId(int clubId) {
        this.clubId = clubId;
    }

    public String getClubSlug() {
        return clubSlug;
    }

    public void setClubSlug(String clubSlug) {
        this.clubSlug = clubSlug;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public String getFocusArea() {
        return focusArea;
    }

    public void setFocusArea(String focusArea) {
        this.focusArea = focusArea;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActivities() {
        return activities;
    }

    public void setActivities(String activities) {
        this.activities = activities;
    }

    public String getIconClass() {
        return (iconClass == null || iconClass.isBlank()) ? "bi-stars" : iconClass;
    }

    public void setIconClass(String iconClass) {
        this.iconClass = iconClass;
    }

    public String getAccentTheme() {
        return (accentTheme == null || accentTheme.isBlank()) ? "indigo" : accentTheme;
    }

    public void setAccentTheme(String accentTheme) {
        this.accentTheme = accentTheme;
    }

    public Integer getCoordinatorId() {
        return coordinatorId;
    }

    public void setCoordinatorId(Integer coordinatorId) {
        this.coordinatorId = coordinatorId;
    }

    public String getCoordinatorName() {
        return coordinatorName;
    }

    public void setCoordinatorName(String coordinatorName) {
        this.coordinatorName = coordinatorName;
    }

    public Integer getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Integer leaderId) {
        this.leaderId = leaderId;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public int getEventCount() {
        return eventCount;
    }

    public void setEventCount(int eventCount) {
        this.eventCount = eventCount;
    }

    public boolean isUserMember() {
        return userMember;
    }

    public void setUserMember(boolean userMember) {
        this.userMember = userMember;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
