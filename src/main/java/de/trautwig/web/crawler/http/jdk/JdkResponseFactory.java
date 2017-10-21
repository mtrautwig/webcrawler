package de.trautwig.web.crawler.http.jdk;

import de.trautwig.web.crawler.http.Response;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static de.trautwig.web.crawler.http.IOUtils.consumeFully;

public class JdkResponseFactory {

    public Response createResponse(HttpURLConnection connection, Instant started) throws IOException {
        Map<String, List<String>> requestHeaders = connection.getRequestProperties();

        connection.connect();
        return new Response.Builder()
                .responseCode(connection.getResponseCode())
                .responseMessage(connection.getResponseMessage())
                .url(connection.getURL())
                .requestHeaders(requestHeaders)
                .headers(connection.getHeaderFields())
                .body(extractEntity(connection))
                .timing(started)
                .build();
    }

    private byte[] extractEntity(HttpURLConnection connection) throws IOException {
        try (InputStream inputStream = getInputStream(connection)) {
            if (inputStream == null) {
                return null;
            }
            return consumeFully(inputStream);
        }
    }

    private InputStream getInputStream(HttpURLConnection connection) throws IOException {
        if (connection.getResponseCode() >= 400) {
            return connection.getErrorStream();
        } else {
            return connection.getInputStream();
        }
    }
}
