package com.taskmanagement.enums;

public enum UserRole {
    ADMIN("ROLE_ADMIN", "System Administrator"),
    CEO("ROLE_CEO", "Chief Executive Officer"),
    MANAGER("ROLE_MANAGER", "Manager"),
    OFFICER("ROLE_OFFICER", "Officer");

    private final String authority;
    private final String displayName;

    UserRole(String authority, String displayName) {
        this.authority = authority;
        this.displayName = displayName;
    }

    public String getAuthority() {
        return authority;
    }

    public String getDisplayName() {
        return displayName;
    }
}
