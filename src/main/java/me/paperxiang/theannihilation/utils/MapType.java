package me.paperxiang.theannihilation.utils;
public enum MapType {
    UNDERGROUND("underground"), SURFACE("surface"), NETHER("nether");
    private final String id;
    MapType(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
}
