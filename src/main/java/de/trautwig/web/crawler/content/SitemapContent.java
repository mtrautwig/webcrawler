package de.trautwig.web.crawler.content;

import de.trautwig.web.crawler.http.Response;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

public class SitemapContent extends Content {
    public SitemapContent() {
        super("application", "xml");
    }

    @Override
    public boolean supports(Response response) {
        return super.supports(response) && response.getUrl().getPath().endsWith("sitemap.xml");
    }

    @Override
    public Stream<String> extractLinks(Response response) throws IOException {
        try (InputStream in = response.getData().getInputStream()) {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
            XPath xpath = XPathFactory.newInstance().newXPath();

            NodeList result = (NodeList) xpath.evaluate("/urlset/url/loc", doc, XPathConstants.NODESET);
            return stream(result);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private Stream<String> stream(NodeList list) {
        Stream.Builder<String> builder = Stream.builder();
        for(int i=0; i < list.getLength(); i++) {
            builder.add(list.item(i).getTextContent());
        }
        return builder.build();
    }
}
