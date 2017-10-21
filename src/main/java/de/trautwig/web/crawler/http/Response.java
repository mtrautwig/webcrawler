package de.trautwig.web.crawler.http;

import javax.activation.DataSource;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Response {
    private int responseCode;
    private String responseMessage;
    private URL url;
    private Map<String, List<String>> requestHeaders;
    private Map<String, List<String>> headers;
    private Map<String, String> attributes = new HashMap<>();
    private byte[] data;
    private Duration timing;

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public boolean isRedirect() {
        return responseCode == 301 || responseCode == 302;
    }

    public boolean isFailure() {
        return responseCode / 100 >= 4;
    }

    public Map<String, List<String>> getRequestHeaders() {
        return Collections.unmodifiableMap(requestHeaders);
    }

    public Map<String, List<String>> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public Optional<String> getHeader(String name) {
        return Optional.ofNullable(headers.get(name.toLowerCase(Locale.ENGLISH)))
                .map(v -> v.isEmpty() ? null : v.get(0));
    }

    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public String getLocation() {
        return getHeader("location").orElse(null);
    }

    public MimeType getContentType() {
        MimeType mimeType;
        try {
            Optional<String> contentType = getHeader("content-type");
            if (contentType.isPresent()) {
                mimeType = new MimeType(contentType.get());
            } else {
                mimeType = null;
            }
        } catch (MimeTypeParseException e) {
            mimeType = new MimeType();
            mimeType.setParameter("error", e.getMessage());
        }
        return mimeType;
    }

    public URL getUrl() {
        return url;
    }

    public DataSource getData() {
        return new DataSource() {

            @Override
            public InputStream getInputStream() throws IOException {
                return Response.this.data == null ? null : new ByteArrayInputStream(Response.this.data);
            }

            @Override
            public OutputStream getOutputStream() throws IOException {
                throw new IOException("Unsupported");
            }

            @Override
            public String getContentType() {
                return Response.this.getContentType().toString();
            }

            @Override
            public String getName() {
                return null;
            }
        };
    }

    public Duration getTiming() {
        return timing;
    }

    public static class Builder {
        Response response = new Response();

        public Response build() {
            return response;
        }

        public Builder responseCode(int responseCode) {
            response.responseCode = responseCode;
            return this;
        }

        public Builder responseMessage(String responseMessage) {
            response.responseMessage = responseMessage;
            return this;
        }

        public Builder url(URL url) {
            response.url = url;
            return this;
        }

        public Builder requestHeaders(Map<String, List<String>> headerFields) {
            response.requestHeaders = new HashMap<>();
            for (Map.Entry<String, List<String>> header : headerFields.entrySet()) {
                if (header.getKey() != null) {
                    response.requestHeaders.put(header.getKey().toLowerCase(Locale.ENGLISH), header.getValue());
                }
            }
            return this;
        }

        public Builder headers(Map<String, List<String>> headerFields) {
            response.headers = new HashMap<>();
            for (Map.Entry<String, List<String>> header : headerFields.entrySet()) {
                if (header.getKey() != null) {
                    response.headers.put(header.getKey().toLowerCase(Locale.ENGLISH), header.getValue());
                }
            }
            return this;
        }

        public Builder attribute(String name, String value) {
            response.attributes.put(name, value);
            return this;
        }

        public Builder body(byte[] data) throws IOException {
            response.data = data;
            return this;
        }

        public Builder timing(Instant started) {
            response.timing = Duration.between(started, Instant.now());
            return this;
        }
    }
}
