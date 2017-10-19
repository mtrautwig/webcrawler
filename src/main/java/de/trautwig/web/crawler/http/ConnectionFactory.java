package de.trautwig.web.crawler.http;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

@Component
public class ConnectionFactory {

    public HttpURLConnection createRequest(String method, URI uri) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(false);
        //connection.setDoOutput(true);
        //connection.setChunkedStreamingMode(0); // enforces output!
        connection.setDoInput(true);
        connection.setRequestMethod(method);
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("User-Agent", "WebCrawler/1.0");
        return connection;
    }

}
