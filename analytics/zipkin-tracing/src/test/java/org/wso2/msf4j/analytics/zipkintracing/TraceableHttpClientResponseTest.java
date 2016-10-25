package org.wso2.msf4j.analytics.zipkintracing;

import com.github.kristofa.brave.http.HttpResponse;
import feign.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;

/**
 * Class for testing TraceableHttpClientResponse.
 */
public class TraceableHttpClientResponseTest extends Assert {

    private Response response;
    private HttpResponse httpResponse;

    @BeforeClass
    public void setUp() throws IOException {
        response = Response.builder().status(200).headers(new HashMap<>()).build();
        httpResponse = new TraceableHttpClientResponse(response);
    }

    @Test
    public void testGetStatusCode() {
        assertEquals(httpResponse.getHttpStatusCode(), response.status());
    }

}
