package de.trautwig.web.crawler.content;

import de.trautwig.web.crawler.http.Response;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.activation.DataSource;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HtmlContentTest {

    Response mockResponse;

    @Before
    public void setUp() throws IOException {
        mockResponse = mockResponse("/html/test.html");
    }

    @Test
    public void htmlLinksAreDetected() throws IOException {
        List<String> links = new HtmlContent().extractLinks(mockResponse).collect(toList());
        assertThat("styles.css", isIn(links));
        assertThat("link.html", isIn(links));
        assertThat("image-element.png", isIn(links));
    }

    @Test
    public void styleLinksAreDetected() throws IOException {
        List<String> links = new HtmlContent().extractLinks(mockResponse).collect(toList());
        assertThat("page-background.png", isIn(links));
        assertThat("inline-styled.png", isIn(links));
    }

    @Test
    @Ignore("not yet supported")
    public void formLinksAreDetected() throws IOException {
        List<String> links = new HtmlContent().extractLinks(mockResponse).collect(toList());
        assertThat("form-submit", isIn(links));
    }

    static Response mockResponse(String resource) throws IOException {
        DataSource mockData = mock(DataSource.class);
        when(mockData.getInputStream()).thenReturn(HtmlContentTest.class.getResourceAsStream(resource));

        Response mockResponse = mock(Response.class);
        when(mockResponse.getUrl()).thenReturn(new URL("http://example.com"));
        when(mockResponse.getEncoding()).thenReturn(null);
        when(mockResponse.getData()).thenReturn(mockData);
        return mockResponse;
    }
}
