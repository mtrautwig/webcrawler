package de.trautwig.web.crawler.http;

import java.net.URI;
import java.net.URISyntaxException;

public class Request {

    private String method;
    private URI uri;
    private URI referer;
    private int distance;

    public static Request get(URI uri) {
        return new Request("GET", uri);
    }

    public static Request head(URI uri) {
        return new Request("HEAD", uri);
    }

    Request(String method, URI uri) {
        this.method = method;
        this.uri = uri;
    }

    public Request withOrigin(Response origin) {
        try {
            this.referer = origin.getUrl().toURI();
        } catch (URISyntaxException e) {
            // silently ignore
        }
        return this;
    }

    public Request withSource(Request source) {
        this.distance = source.getDistance() + 1;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public URI getURI() {
        return uri;
    }

    public URI getReferer() {
        return referer;
    }

    public int getDistance() {
        return distance;
    }
}
