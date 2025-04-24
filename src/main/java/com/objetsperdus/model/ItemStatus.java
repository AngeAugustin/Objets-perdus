package com.objetsperdus.model;

public enum ItemStatus {
    PENDING("En attente de validation"),
    APPROVED("Approuvé"),
    REJECTED("Rejeté"),
    CLAIMED("Réclamé"),
    RETURNED("Rendu au propriétaire");

    private final String displayName;

    ItemStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}