Simple Web Crawler
==================

This is a simple framework for building Web Crawlers in order to find dead
links on Web sites. Starting from a given set of URLs ("seeds"), it downloads 
the  contents and follows all links, where this process starts again. Any 
request and the response is logged (omitting the body), either on the console
or in JUnit/Surefire format (we let Jenkins build HTML reports).

See the ``WebCrawler`` class for an example invocation. By default, 10 threads
are used in parallel and cookies are supported across all requests. The 
``LinkPolicy`` class decides whether a link is followed or not - according to 
a list of white-listed servers and excluded paths, and the distance from the 
seed, and whether the page has already been in the same run.
The ``LinkExtractor`` class normalizes the links extracted from responses and
turns them into new requests. Certain files are downloaded (GET), others are
only tested (HEAD) for existence. 

Currently supported content:
* **HTML pages**. Any link found in a ``src`` or ``href`` attribute is 
  followed.
* **Sitemap XML files** containing links to all public pages.

 