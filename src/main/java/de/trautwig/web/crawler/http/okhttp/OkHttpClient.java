package de.trautwig.web.crawler.http.okhttp;

import de.trautwig.web.crawler.http.Client;
import de.trautwig.web.crawler.http.Request;
import de.trautwig.web.crawler.http.Response;
import okhttp3.Headers;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static de.trautwig.web.crawler.http.IOUtils.consumeFully;

/**
 * HTTP Client using OkHttp, which is HTTP/2 capable
 */
public class OkHttpClient implements Client {
    private final okhttp3.OkHttpClient delegate;

    CookieManager cookieManager = new CookieManager();

    public OkHttpClient() {
        this.delegate = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .followRedirects(false)
                .followSslRedirects(false)
                .build();
    }

    @Override
    public Response execute(Request request) throws IOException {
        Instant started = Instant.now();
        okhttp3.Request delegateRequest = toOkHttpRequest(request);
        okhttp3.Response delegateResponse = delegate.newCall(delegateRequest).execute();
        return toResponse(delegateRequest, delegateResponse, started);
    }

    private okhttp3.Request toOkHttpRequest(Request request) throws IOException {
        Headers.Builder headers = new Headers.Builder();
        headers.add("Accept", "*/*");
        headers.add("User-Agent", "WebCrawler/1.0");

        if (request.getReferer() != null) {
            headers.add("Referer", request.getReferer().toString());
        }
        applyCookies(request.getURI(), headers);

        return new okhttp3.Request.Builder()
                .url(request.getURI().toURL())
                .headers(headers.build())
                .method(request.getMethod(), null)
                .build();
    }

    private Response toResponse(okhttp3.Request delegateRequest, okhttp3.Response delegateResponse, Instant started) throws IOException {
        updateCookies(delegateRequest.url().uri(), delegateResponse.headers());
        return new Response.Builder()
                .url(delegateRequest.url().url())
                .responseCode(delegateResponse.code())
                .responseMessage(delegateResponse.message())
                .requestHeaders(delegateRequest.headers().toMultimap())
                .headers(delegateResponse.headers().toMultimap())
                .body(extractEntity(delegateResponse.body()))
                .attribute("Protocol", delegateResponse.protocol().toString())
                .timing(started)
                .build();
    }

    private void applyCookies(URI uri, Headers.Builder headers) throws IOException {
        Map<String, List<String>> cookies = new HashMap<>();
        synchronized (cookieManager) {
            cookies = cookieManager.get(uri, cookies);
        }
        cookies.entrySet().forEach(header -> {
            header.getValue().forEach(value -> {
                headers.add(header.getKey(), value);
            });
        });
    }

    private void updateCookies(URI uri, Headers headers) throws IOException {
        synchronized (cookieManager) {
            cookieManager.put(uri, headers.toMultimap());
        }
    }

    /**
     * The bytes() method is pretty picky when the server falsely responds with "Content-Length: 0"...
     */
    private byte[] extractEntity(ResponseBody body) throws IOException {
        try (InputStream inputStream = body.byteStream()) {
            if (inputStream == null) {
                return null;
            }
            return consumeFully(inputStream);
        }
    }
}
