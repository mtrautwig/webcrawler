package de.trautwig.web.crawler.content;

import de.trautwig.web.crawler.http.Response;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.io.IOException;
import java.util.stream.Stream;

public abstract class Content {

    private final MimeType mimeType;

    Content(String primary, String sub) {
        try {
            mimeType = new MimeType(primary, sub);
        } catch (MimeTypeParseException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean supports(Response response) {
        return mimeType.match(response.getContentType());
    }

    public abstract Stream<String> extractLinks(Response response) throws IOException;

}
