package de.trautwig.web.crawler.content;

import de.trautwig.web.crawler.http.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

public class HtmlContent extends Content {
    public HtmlContent() {
        super("text", "html");
    }

    public Stream<String> extractLinks(Response response) throws IOException {
        try (InputStream in = response.getData().getInputStream()) {
            if (in == null) {
                return Stream.empty();
            }

            String charset = response.getContentType().getParameter("charset");
            Document doc = Jsoup.parse(in, charset, response.getUrl().toString());

            return Stream.concat(
                    doc.select("*[href]").stream()
                            .map(el -> el.attr("href")),
                    doc.select("*[src]").stream()
                            .map(el -> el.attr("src")));
        }
    }

}
