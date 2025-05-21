package com.lifelover.dome.core.helpers;

import com.google.gson.Gson;

public class JsonUtil {
    private static final Gson gson = new Gson();

    private JsonUtil(){
        
    }


    public static String toJson(Object obj){
        return gson.toJson(obj);
    }


    public static <T> T fromJson(String json, Class<T> clz){
        return gson.fromJson(json, clz);
    }
}
