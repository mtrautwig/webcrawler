package de.trautwig.web.crawler.link;

import de.trautwig.web.crawler.content.Content;
import de.trautwig.web.crawler.content.HtmlContent;
import de.trautwig.web.crawler.content.SitemapContent;
import de.trautwig.web.crawler.http.Request;
import de.trautwig.web.crawler.http.Response;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class LinkExtractor {

    private List<Content> contents = Arrays.asList(new HtmlContent(), new SitemapContent());

    /** for these extensions we want to use HEAD instead of GET */
    private Set<String> noDownloadExtensions = new HashSet<>(Arrays.asList(
            ".css", ".js", ".png", ".jpg", ".jpeg", ".gif", ".ico", ".bmp", ".pdf", ".doc", ".docx", ".exe", ".msi"
    ));

    public List<Request> toRequests(Request request, Response response) {
        if (response.isRedirect() && response.getLocation() != null) {
            URI redirectUri = normalizeUrl(response.getUrl(), response.getLocation());
            if (redirectUri != null) {
                return Collections.singletonList(createRequest(redirectUri, request, response));
            }
        }

        return contents.stream()
                .filter(c -> c.supports(response))
                .findFirst()
                .map(c -> {
                    try {
                        return c.extractLinks(response);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(links -> toRequests(request, response, links))
                .orElse(Collections.emptyList());
    }

    private List<Request> toRequests(Request request, Response response, Stream<String> links) {
        return links.filter(link -> StringUtils.hasText(link))
                .map(link -> normalizeUrl(response.getUrl(), link))
                .filter(this::schemeSupported)
                .map(url -> createRequest(url, request, response))
                .collect(Collectors.toList());

    }

    private URI normalizeUrl(URL base, String href) {
        try {
            return new URL(base, URLDecoder.decode(href, "UTF-8")).toURI();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            return null;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private boolean schemeSupported(URI uri) {
        return uri != null && ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()));
    }

    private Request createRequest(URI uri, Request request, Response response) {
        if (uri.getPath() != null) {
            if (noDownloadExtensions.contains(uri.getPath().toLowerCase().replaceFirst(".+([.][^/\\.]+)", "$1"))) {
                return Request.head(uri).withSource(request).withOrigin(response);
            }
        }
        return Request.get(uri).withSource(request).withOrigin(response);
    }
}
