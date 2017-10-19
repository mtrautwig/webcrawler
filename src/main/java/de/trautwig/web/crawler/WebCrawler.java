package de.trautwig.web.crawler;

import de.trautwig.web.crawler.http.Client;
import de.trautwig.web.crawler.http.Request;
import de.trautwig.web.crawler.http.Response;
import de.trautwig.web.crawler.link.LinkExtractor;
import de.trautwig.web.crawler.link.LinkPolicy;
import de.trautwig.web.crawler.log.JUnitResponseLogger;
import de.trautwig.web.crawler.log.ResponseLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.net.CookieManager;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

@SpringBootApplication
public class WebCrawler implements CommandLineRunner {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(WebCrawler.class, args);
    }

    @Autowired
    ForkJoinPool executor;

    @Autowired
    Client client;

    @Autowired
    LinkExtractor links;

    @Autowired
    LinkPolicy policy;

    @Autowired
    ResponseLogger logger;

    @Override
    public void run(String... args) throws Exception {
        List<Request> seed = new LinkedList<>();
        seed.add(Request.get(URI.create("https://example.com/")));
        seed.add(Request.get(URI.create("https://example.com/sitemap.xml")));

        seed.forEach(r -> policy.addAllowedServer(r.getURI()));
        policy.addBlockedPath("/forum/");

        seed.forEach(request -> executor.invoke(execute(request)));
        logger.close();
    }

    private RecursiveAction execute(Request request) {
        return new RecursiveAction() {
            @Override
            protected void compute() {
                if (policy.isAllowed(request)) {
                    try {
                        Response response = client.execute(request);
                        invokeAll(links.toRequests(request, response).stream()
                                .map(WebCrawler.this::execute).collect(Collectors.toList()));
                    } catch (Exception e) {
                        logger.log(request, e);
                    }
                }
            }
        };
    }

    @Bean
    ResponseLogger responseLogger() throws Exception {
        return new JUnitResponseLogger(new File("TEST-crawler.xml"));
    }

    @Bean
    CookieManager cookieManager() {
        return new CookieManager();
    }

    @Bean
    ForkJoinPool executorService() {
        return new ForkJoinPool(10);
    }
}
