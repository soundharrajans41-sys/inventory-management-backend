package com.inventory.entity;

public enum Role {
    ADMIN,
    STAFF,
    VIEWER;

    public static Role fromString(String value) {
        if (value == null) return VIEWER;
        String upper = value.trim().toUpperCase();
        if (upper.equals("ROLE_ADMIN") || upper.equals("ADMIN")) return ADMIN;
        if (upper.equals("ROLE_STAFF") || upper.equals("STAFF") || upper.equals("MANAGER")) return STAFF;
        if (upper.equals("ROLE_VIEWER") || upper.equals("VIEWER")) return VIEWER;
        return VIEWER;
    }
}
