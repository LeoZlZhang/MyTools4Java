package leo.tools.object_convert;


import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Optional;

@SuppressWarnings({"unused", "unchecked", "WeakerAccess"})
public final class JacksonKits {
    private static final ObjectMapper mapper = new ObjectMapper()
            .disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);


    public static <T> Optional<T[]> fromJsonArray(String sourceStr, Class<T> cls) {
        if (sourceStr == null || sourceStr.isEmpty())
            return Optional.empty();

        try {
            return Optional.ofNullable((T[]) mapper.readValue(sourceStr, Array.newInstance(cls, 0).getClass()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Optional<T> fromJson(String sourceStr, Class<T> cls) {
        if (sourceStr == null || sourceStr.isEmpty())
            return Optional.empty();

        try {
            return Optional.ofNullable(mapper.readValue(sourceStr, cls));
        } catch (IOException e) {
            throw new RuntimeException(String.format("fail to transfer json object from [\n%s\n] to %s", sourceStr, cls.getSimpleName()));
        }
    }

    private static <T> Optional<T> fromJson(InputStream is, Class<T> cls) {
        try {
            return Optional.ofNullable(mapper.readValue(is, cls));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toPrettyJson(Object obj) {
        try {
            if (obj instanceof String)
                obj = mapper.readValue(obj.toString(), Object.class);
            return "\r\n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isJsonObject(String str) {
        try {
            new JSONObject(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isJsonArray(String str) {
        try {
            new JSONArray(str);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    public static <T, R> Optional<R> fromObject2Object(T from, Class<R> to) {
        return fromJson(toJson(from), to);
    }
}
