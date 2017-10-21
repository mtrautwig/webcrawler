package de.trautwig.web.crawler.log;

import de.trautwig.web.crawler.http.Request;
import de.trautwig.web.crawler.http.Response;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class JUnitResponseLogger implements ResponseLogger {

    private final XMLStreamWriter writer;

    public JUnitResponseLogger(File outputFile) throws FileNotFoundException, XMLStreamException {
        writer = XMLOutputFactory.newInstance().createXMLStreamWriter(new FileOutputStream(outputFile));
        writer.writeStartDocument();
        writer.writeStartElement("testsuite");
        writer.writeAttribute("name", "WebCrawler");
    }

    @Override
    public void log(Request request, Response response) {
        try {
            synchronized (writer) {
                writer.writeStartElement("testcase");
                writer.writeAttribute("name", request.getURI().toString());
                writer.writeAttribute("time", String.format(Locale.ENGLISH, "%.3f", response.getTiming().toMillis() / 1000.0));
                if (response.isFailure()) {
                    writer.writeEmptyElement("failure");
                    writer.writeAttribute("message", String.valueOf(response.getResponseCode())
                            .concat(" ").concat(response.getResponseMessage()));
                }

                writer.writeStartElement("system-out");
                writer.writeCData(formatExchange(request, response).toString());

                writer.writeEndElement();
                writer.writeEndElement();
            }
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void log(Request request, Throwable t) {
        try {
            synchronized (writer) {
                writer.writeStartElement("testcase");
                writer.writeAttribute("name", request.getURI().toString());
                writer.writeAttribute("time", String.format(Locale.ENGLISH, "%.3f", 0.0));
                writer.writeEmptyElement("error");
                writer.writeAttribute("message", t.getMessage());
                writer.writeAttribute("type", t.getClass().toString());
                writer.writeStartElement("system-err");

                StringWriter buffer = new StringWriter();
                t.printStackTrace(new PrintWriter(buffer));
                writer.writeCData(buffer.toString());

                writer.writeEndElement();
                writer.writeEndElement();
            }
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() throws IOException {
        try {
            writer.writeEndDocument();
            writer.close();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    String formatExchange(Request request, Response response) {
        StringBuilder responseStr = new StringBuilder()
                .append(request.getMethod()).append(" ").append(request.getURI()).append("\n")
                .append(formatHeaders(response.getRequestHeaders()))
                .append("\n")
                .append(response.getResponseCode()).append(" ").append(response.getResponseMessage()).append("\n")
                .append(formatHeaders(response.getHeaders()))
                .append("\n")
                .append("Referer = ").append(request.getReferer()).append("\n")
                .append("Distance = ").append(request.getDistance()).append("\n")
                .append("Timing = ").append(response.getTiming().toString()).append("\n");

        for (Map.Entry<String, String> attr : response.getAttributes().entrySet()) {
            responseStr.append(attr.getKey()).append(" = ").append(attr.getValue());
        }

        return responseStr.toString();
    }

    String formatHeaders(Map<String, List<String>> headers) {
        return headers.entrySet()
                .stream()
                .flatMap(e -> e.getValue().stream().map(v -> e.getKey() + ": " + v + "\n"))
                .collect(Collectors.joining());
    }

}
