package de.trautwig.web.crawler.http;

import de.trautwig.web.crawler.log.ResponseLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class Client {

    @Autowired
    CookieManager cookieManager;

    @Autowired
    ConnectionFactory connectionFactory;

    @Autowired
    ResponseFactory responseFactory;

    @Autowired
    ResponseLogger logger;

    public Response execute(Request request) throws IOException {
        if (!isSupported(request.getURI())) {
            throw new IOException("Unsupported uri: " + request.getURI());
        }

        HttpURLConnection connection = connectionFactory.createRequest(request.getMethod(), request.getURI());
        applyCookies(request.getURI(), connection);
        if (request.getReferer() != null) {
            connection.addRequestProperty("Referer", request.getReferer().toString());
        }

        Instant started = Instant.now();

        connection.connect();
        try {
            Response response = responseFactory.createResponse(connection, started);
            logger.log(request, response);
            return response;
        } finally {
            updateCookies(request.getURI(), connection);
        }
    }

    private boolean isSupported(URI uri) {
        return "http".equals(uri.getScheme()) || "https".equals(uri.getScheme());
    }

    private void applyCookies(URI uri, HttpURLConnection connection) throws IOException {
        Map<String, List<String>> cookies;
        synchronized (cookieManager) {
            cookies = cookieManager.get(uri, connection.getRequestProperties());
        }
        cookies.entrySet().forEach(header -> {
            header.getValue().forEach(value -> {
                connection.addRequestProperty(header.getKey(), value);
            });
        });
    }

    private void updateCookies(URI uri, HttpURLConnection connection) throws IOException {
        synchronized (cookieManager) {
            cookieManager.put(uri, connection.getHeaderFields());
        }
    }
}
