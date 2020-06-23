package bladedragon.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class JsonUtil {
    private static final Gson gson = new Gson();

    public static String toJson(Object object) {
        return new Gson().toJson(object);
    }

    public static <T> T fromJson(String jsonStr,Class<T> clazz){
        return gson.fromJson(jsonStr,clazz);
    }

    public static JsonObject fromJson (String jsonStr) {
        return gson.fromJson(jsonStr, JsonObject.class);
    }
}
