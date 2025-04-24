package com.objetsperdus.model;

public enum ItemCategory {
    ELECTRONIQUE("Électronique"),
    VETEMENT("Vêtement"),
    BIJOU("Bijou"),
    DOCUMENT("Document"),
    ACCESSOIRE("Accessoire"),
    CLE("Clé"),
    ARGENT("Argent"),
    ANIMAL("Animal"),
    AUTRE("Autre");

    private final String displayName;

    ItemCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}