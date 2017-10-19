package de.trautwig.web.crawler.log;

import de.trautwig.web.crawler.http.Request;
import de.trautwig.web.crawler.http.Response;

public class ConsoleResponseLogger implements ResponseLogger {

    public void log(Request request, Response response) {
        System.out.println(request.getMethod() + "\t" +
                request.getURI() + "\t" +
                response.getResponseCode() + " " +
                response.getResponseMessage() + "\t" +
                String.format("%.3fs\t", response.getTiming().toMillis() / 1000.0) +
                response.getContentType() + "\t(" +
                request.getReferer() + ")");
    }

    @Override
    public void log(Request request, Throwable t) {
        System.err.print("ERROR " + request.getMethod() + " " + request.getURI() + " " + request.getReferer() + ": ");
        t.printStackTrace(System.err);
    }

    @Override
    public void close() throws Exception {
    }

}
