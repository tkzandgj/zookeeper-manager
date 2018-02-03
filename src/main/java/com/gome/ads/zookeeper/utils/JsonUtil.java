package com.gome.ads.zookeeper.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

/**
 * JSon处理工具类
 * @author wuxuefei
 */
public class JsonUtil {
    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public static String objectToString(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T stringToObject(String str, Class<T> tClass) {
        return gson.fromJson(str, tClass);
    }

    public static <T> T stringToObject(byte[] bytes, Class<T> tClass) throws UnsupportedEncodingException {
	    return gson.fromJson(new String(bytes, "UTF-8"), tClass);
    }
    
    public static <T> T stringToObject(String str, Type type) {
        return gson.fromJson(str, type);
    }
}
