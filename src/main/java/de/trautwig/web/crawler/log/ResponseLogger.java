package de.trautwig.web.crawler.log;

import de.trautwig.web.crawler.http.Request;
import de.trautwig.web.crawler.http.Response;

public interface ResponseLogger extends AutoCloseable {

    void log(Request request, Response response);

    void log(Request request, Throwable t);

}
