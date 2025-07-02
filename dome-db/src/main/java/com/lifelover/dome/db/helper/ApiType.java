package com.lifelover.dome.db.helper;

public enum ApiType {
    INT("服务提供的接口"),
    EXT("调用外部的接口");

    private final String description;

    ApiType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static ApiType of(String name){
        return valueOf(name.toUpperCase());
    }
}
