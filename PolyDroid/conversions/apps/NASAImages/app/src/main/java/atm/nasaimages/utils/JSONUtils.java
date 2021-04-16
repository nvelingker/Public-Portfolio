package atm.nasaimages.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

public class JSONUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static <T> T fromJSON_URL(String url, Class<T> type) {
        T res = null;
        try {
            res = MAPPER.readValue(new URL(url), type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static <T> T fromJSON_URL(String baseUrl, String[] params, String[] vals, Class<T> type) {
        String url = encodeURL(baseUrl, params, vals);
        return fromJSON_URL(url, type);
    }

    private static String encodeURL(String urlString, String[] params, String[] vals) {
        if (params.length != vals.length) {
            throw new IllegalArgumentException("Both arrays must have the same amount of elements");
        }
        String res = "";
        try {
            String urlStringParams = urlString + "?";
            for (int i = 0; i < params.length; i++) {
                urlStringParams += params[i] + "=" + URLEncoder.encode(vals[i], "UTF-8");
                if (i < params.length - 1) {
                    urlStringParams += "&";
                }
            }
            res = urlStringParams;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return res;
    }

}
