package de.trautwig.web.crawler.link;

import de.trautwig.web.crawler.http.Request;
import de.trautwig.web.crawler.log.PolicyLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

@Component
public class LinkPolicy {

    Set<String> allowedServers = new HashSet<>();
    Set<String> blockedPaths = new HashSet<>();

    int maxDistance = 20;

    @Autowired
    VisitCache visits;

    @Autowired
    PolicyLogger logger;

    public void addAllowedServer(URI uri) {
        allowedServers.add(uri.getHost());
    }

    public void addBlockedPath(String path) {
        this.blockedPaths.add(path);
    }

    public void setMaxDistance(int maxDistance) {
        this.maxDistance = maxDistance;
    }

    public boolean isAllowed(Request request) {
        URI uri = request.getURI();

        if (!allowedServers.contains(uri.getHost())) {
            logger.forbidden(uri, "server-forbidden");
            return false;
        }

        if (request.getDistance() > maxDistance) {
            logger.forbidden(uri, "distance-exceeded");
            return false;
        }

        if (alreadyVisited(uri)) {
            logger.forbidden(uri, "already-visited");
            return false;
        }

        for (String blockedPath : blockedPaths) {
            if (uri.getPath().startsWith(blockedPath)) {
                logger.forbidden(uri, "path-forbidden");
                return false;
            }
        }

        logger.allowed(uri);
        return true;
    }

    private boolean alreadyVisited(URI uri) {
        return !visits.putIfAbsent(uri);
    }
}
