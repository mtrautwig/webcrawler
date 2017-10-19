package de.trautwig.web.crawler.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class PolicyLogger {
    private Logger logger = LoggerFactory.getLogger("POLICY");

    public void allowed(URI uri) {
        logger.info("ALLOW {}", uri);
    }

    public void forbidden(URI uri, String reason) {
        logger.info("DENY:{} {}", reason, uri);
    }

}
