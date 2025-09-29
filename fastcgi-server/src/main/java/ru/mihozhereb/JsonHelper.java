package ru.mihozhereb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JsonHelper {
    public static class RequestData {
        Double x;
        Double y;
        Integer r;
    }

    public static class ResponseData {
        Double x;
        Double y;
        Integer r;
        Boolean hit;
        LocalDateTime time;
        Long timing;

        public ResponseData(Double x, Double y, Integer r, Boolean hit, LocalDateTime time, Long timing) {
            this.x = x;
            this.y = y;
            this.r = r;
            this.hit = hit;
            this.time = time;
            this.timing = timing;
        }
    }

    private final static Gson gson = new Gson();

    public static RequestData deserializeRequest(String json) throws IOException {
        if (json == null || json.isBlank()) {
            throw new IOException("Blank string");
        }
        try {
            RequestData d = gson.fromJson(json, RequestData.class);

            if (d == null || d.x == null || d.y == null || d.r == null) {
                throw new IOException("Not all required fields found: x, y, r");
            }
            if (d.x < -5 || d.x > 3) {
                throw new IOException("X is out of range [-5;3]");
            }
            if (d.y < -3 || d.y > 5) {
                throw new IOException("Y is out of range [-3;3]");
            }
            if (!(d.r == 1 || d.r == 2 || d.r == 3 || d.r == 4 || d.r == 5)) {
                throw new IOException("R must be one of {1,2,3,4,5}");
            }

            return d;
        } catch (JsonSyntaxException e) {
            throw new IOException(e.getMessage());
        }
    }

    public static String serializeResponse(ResponseData response) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (com.google.gson.JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                                src == null ? null :
                                        new com.google.gson.JsonPrimitive(
                                                src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                        ))
                .setPrettyPrinting()
                .create();

        return gson.toJson(response);
    }
}
