package de.trautwig.web.crawler.content;

import de.trautwig.web.crawler.http.Response;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

public class CssContent extends Content {

    public CssContent() {
        super("text", "css");
    }

    @Override
    public Stream<String> extractLinks(Response response) throws IOException {
        String charset = ofNullable(response.getEncoding()).orElse(Charset.defaultCharset().toString());
        try (Reader reader = new InputStreamReader(response.getData().getInputStream(), charset)) {
            return CssParser.fromStylesheet(reader).getResult();
        }
    }

    @Override
    public boolean supports(Response response) {
        return super.supports(response) || response.getUrl().getPath().endsWith(".css");
    }
}
