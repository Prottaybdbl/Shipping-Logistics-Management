package com.taskmanagement.enums;

public enum TaskStatus {
    NOT_STARTED("Not Started", "secondary"),
    IN_PROGRESS("In Progress", "warning"),
    COMPLETED("Completed", "success"),
    STUCK("Stuck", "danger");

    private final String displayName;
    private final String badgeClass;

    TaskStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
