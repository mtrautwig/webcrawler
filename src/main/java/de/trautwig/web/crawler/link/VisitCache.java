package de.trautwig.web.crawler.link;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VisitCache {

    private Map<URI, Boolean> visited = new ConcurrentHashMap<>();

    public boolean putIfAbsent(URI uri) {
        return visited.putIfAbsent(normalize(uri), Boolean.TRUE) == null;
    }

    private URI normalize(URI uri) {
        try {
            return new URI("http", null, uri.getHost(), 80, uri.getPath(), uri.getQuery(), null).normalize();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
