package de.trautwig.web.crawler.content;

import de.trautwig.web.crawler.http.Response;
import org.junit.Test;

import javax.activation.DataSource;
import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CssContentTest {

    @Test
    public void linksAreDetected() throws IOException {
        Response mockResponse = mockResponse("/css/test.css");

        List<String> links = new CssContent().extractLinks(mockResponse).collect(toList());
        assertThat(links.size(), is(4));
        assertThat("import.css", isIn(links));
        assertThat("url-import.css", isIn(links));
        assertThat("logo.png", isIn(links));
        assertThat("http://www.example.com/redball.png", isIn(links));
    }

    Response mockResponse(String resource) throws IOException {
        DataSource mockData = mock(DataSource.class);
        when(mockData.getInputStream()).thenReturn(getClass().getResourceAsStream(resource));

        Response mockResponse = mock(Response.class);
        when(mockResponse.getEncoding()).thenReturn(null);
        when(mockResponse.getData()).thenReturn(mockData);
        return mockResponse;
    }

}
