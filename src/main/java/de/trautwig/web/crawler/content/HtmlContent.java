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

            String charset = response.getEncoding();
            Document doc = Jsoup.parse(in, charset, response.getUrl().toString());

            Stream.Builder<String> links = Stream.builder();
            addLinksFromCSS(doc, links);
            addLinksFromInlineCSS(doc, links);
            addLinksFromElements(doc, links);
            return links.build();
        }
    }

    private void addLinksFromCSS(Document doc, Stream.Builder<String> links) {
        doc.select("style").forEach(style -> {
            CssParser.fromStylesheet(style.html()).getResult().forEach(links::add);
        });
    }

    private void addLinksFromInlineCSS(Document doc, Stream.Builder<String> links) {
        doc.select("*[style]").forEach(style -> {
            CssParser.fromInline(style.attr("style")).getResult().forEach(links::add);
        });
    }

    private void addLinksFromElements(Document doc, Stream.Builder<String> links) {
        doc.select("*[href]").stream()
                .map(el -> el.attr("href"))
                .forEach(links::add);

        doc.select("*[src]").stream()
                .map(el -> el.attr("src"))
                .forEach(links::add);
    }
}
