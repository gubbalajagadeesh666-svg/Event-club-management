package com.campusflow.model;

/**
 * Aggregated participation statistics row for a single club in Reports.
 */
public class ClubReportRow {

    private int clubId;
    private String clubName;
    private String iconClass;
    private String accentTheme;
    private int activeMembers;
    private int totalEvents;
    private int approvedEvents;
    private int totalRegistrations;
    private int presentAttendance;

    public ClubReportRow() {
    }

    public int getClubId() {
        return clubId;
    }

    public void setClubId(int clubId) {
        this.clubId = clubId;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
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

    public int getActiveMembers() {
        return activeMembers;
    }

    public void setActiveMembers(int activeMembers) {
        this.activeMembers = activeMembers;
    }

    public int getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(int totalEvents) {
        this.totalEvents = totalEvents;
    }

    public int getApprovedEvents() {
        return approvedEvents;
    }

    public void setApprovedEvents(int approvedEvents) {
        this.approvedEvents = approvedEvents;
    }

    public int getTotalRegistrations() {
        return totalRegistrations;
    }

    public void setTotalRegistrations(int totalRegistrations) {
        this.totalRegistrations = totalRegistrations;
    }

    public int getPresentAttendance() {
        return presentAttendance;
    }

    public void setPresentAttendance(int presentAttendance) {
        this.presentAttendance = presentAttendance;
    }
}
