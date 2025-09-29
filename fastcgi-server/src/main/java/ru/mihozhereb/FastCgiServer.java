package ru.mihozhereb;

import com.fastcgi.FCGIInterface;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class FastCgiServer implements Runnable {
    private static final String HTTP_JSON_OK = """
        HTTP/1.1 200 OK
        Content-Type: application/json; charset=UTF-8
        Content-Length: %d

        %s
        """;

    private static final String HTTP_JSON_BAD = """
        HTTP/1.1 400 Bad Request
        Content-Type: application/json; charset=UTF-8
        Content-Length: %d

        %s
        """;

    private static final String HTTP_HTML_OK = """
        HTTP/1.1 200 OK
        Content-Type: text/html; charset=UTF-8
        Content-Length: %d

        %s
        """;

    @Override
    public void run() {
        var fcgiInterface = new FCGIInterface();
        while (fcgiInterface.FCGIaccept() >= 0) {
            var method = System.getProperties().getProperty("REQUEST_METHOD");
            if (method == null) {
                System.out.println(errorResult("Unsupported HTTP method: null"));
                continue;
            }

            if (method.equals("GET")) {
                System.out.println(debugPage());
                continue;
            }

            if (method.equals("POST")) {
                var contentType = System.getProperties().getProperty("CONTENT_TYPE");
                if (contentType == null) {
                    System.out.println(errorResult("Content-Type is null"));
                    continue;
                }

                if (!contentType.equals("application/json") && !contentType.equals("application/json; charset=UTF-8")) {
                    System.out.println(errorResult("Content-Type is not supported"));
                    continue;
                }

                String requestBody;
                try {
                    requestBody = readRequestBody();
                } catch (IOException e) {
                    System.out.println(errorResult(String.format("IOException exception: %s", e)));
                    continue;
                }

                JsonHelper.RequestData requestJson;
                try {
                    requestJson = JsonHelper.deserializeRequest(requestBody);
                } catch (IOException e) {
                    System.out.println(errorResult(String.format("Json parse exception: %s", e)));
                    continue;
                }

                long startTime = System.nanoTime();
                boolean hit = Calculator.isHit(requestJson.x, requestJson.y, requestJson.r);
                long finishTime = System.nanoTime();

                String responseBody = JsonHelper.serializeResponse(new JsonHelper.ResponseData(
                        requestJson.x, requestJson.y, requestJson.r, hit, LocalDateTime.now(), finishTime - startTime
                ));

                System.out.println(okJson(responseBody));
                continue;
            }

            System.out.println(errorResult("Unsupported HTTP method: " + method));
        }
    }

    private static String readRequestBody() throws IOException {
        String lenStr = FCGIInterface.request.params.getProperty("CONTENT_LENGTH");
        int len = 0;
        if (lenStr != null) {
            try { len = Integer.parseInt(lenStr.trim()); } catch (NumberFormatException ignored) {}
        }
        if (len <= 0) return "";

        byte[] buf = new byte[len];
        int read = 0;
        while (read < len) {
            FCGIInterface.request.inStream.fill();
            int r = System.in.read(buf, read, len - read);
            if (r < 0) break;
            read += r;
        }
        return new String(buf, 0, read, StandardCharsets.UTF_8);
    }

    private static String errorResult(String message) {
        String safe = message == null ? "Unknown error" : message;
        String json = "{\"status\":\"error\",\"message\":\"" + safe + "\"}";
        return String.format(HTTP_JSON_BAD, utf8len(json), json);
    }

    private static String okJson(String jsonBody) {
        if (jsonBody == null) jsonBody = "{}";
        return String.format(HTTP_JSON_OK, utf8len(jsonBody), jsonBody);
    }

    private static String debugPage() {
        String html = """
            <!doctype html>
            <html lang="ru"><head>
              <meta charset="utf-8"><title>FastCGI Java</title>
            </head><body>
              <h1>FastCGI Java сервер работает</h1>
              <p>Отправьте POST с <code>Content-Type: application/json</code>.</p>
            </body></html>
            """;
        return String.format(HTTP_HTML_OK, utf8len(html), html);
    }

    private static int utf8len(String s) {
        return s.getBytes(StandardCharsets.UTF_8).length;
    }
}
