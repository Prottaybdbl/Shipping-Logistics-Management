package com.taskmanagement.enums;

/**
 * Status for individual loading/unloading operations
 */
public enum LoadUnloadStatus {
    PENDING("Pending"),
    LOADED("Loaded"),
    UNLOADED("Unloaded"),
    IN_TRANSIT("In Transit"),
    DELIVERED("Delivered");

    private final String displayName;

    LoadUnloadStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
