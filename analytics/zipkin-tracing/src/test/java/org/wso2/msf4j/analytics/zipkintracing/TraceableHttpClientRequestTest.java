package org.wso2.msf4j.analytics.zipkintracing;

import com.github.kristofa.brave.http.HttpClientRequest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.msf4j.delegates.client.MSF4JClientRequestContext;

import java.net.URI;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.ClientRequestContext;

/**
 * Class for testing TraceableHttpClient.
 */
public class TraceableHttpClientRequestTest extends Assert {

    private ClientRequestContext clientRequestContext;
    private HttpClientRequest httpRequest;

    @BeforeClass
    public void setUp() {
        clientRequestContext = new MSF4JClientRequestContext(null, URI.create("msf4j"));
        clientRequestContext.setMethod(HttpMethod.GET);
        httpRequest = new TraceableHttpClientRequest(clientRequestContext);
    }

    @Test
    public void testAddHeader() {
        httpRequest.addHeader("testK", "testV");
        assertEquals(clientRequestContext.getHeaders().getFirst("testK"), "testV");
    }

    @Test
    public void testGetUri() {
        assertEquals(httpRequest.getUri(), URI.create("msf4j"));
    }

    @Test
    public void testGetHttpMethod() {
        assertEquals(httpRequest.getHttpMethod(), HttpMethod.GET);
    }

}
