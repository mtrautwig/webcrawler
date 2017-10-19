package de.trautwig.web.crawler.http;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.time.Instant;

@Component
public class ResponseFactory {

    public Response createResponse(HttpURLConnection connection, Instant started) throws IOException {
        return new Response.Builder()
                .responseCode(connection.getResponseCode())
                .responseMessage(connection.getResponseMessage())
                .url(connection.getURL())
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

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            for (int len = 0; len != -1; len = inputStream.read(buffer)) {
                baos.write(buffer, 0, len);
                // TODO length limit
            }
            return baos.toByteArray();
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
