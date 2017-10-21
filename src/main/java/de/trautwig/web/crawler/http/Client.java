package de.trautwig.web.crawler.http;

import java.io.IOException;

public interface Client {

    Response execute(Request request) throws IOException;

}
