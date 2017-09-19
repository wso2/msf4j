package org.wso2.msf4j.analytics.zipkintracing;

import com.github.kristofa.brave.http.HttpServerRequest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.msf4j.Request;

import java.io.IOException;
import java.net.URI;
import javax.ws.rs.HttpMethod;

/**
 * Class for testing TraceableHttpServerRequest.
 */
public class TraceableHttpServerRequestTest extends Assert {

    private Request request;
    private HttpServerRequest httpServerRequest;

    @BeforeClass
    public void setUp() throws IOException {
        HTTPCarbonMessage httpCarbonMessage = new HTTPCarbonMessage();
        request = new Request(httpCarbonMessage);
        request.getHeaders().set("testK", "testV");
        request.setProperty("TO", "msf4j");
        request.setProperty("HTTP_METHOD", HttpMethod.GET);
        httpServerRequest = new TraceableHttpServerRequest(request);
    }

    @Test
    public void testGetHeader() {
        assertEquals(httpServerRequest.getHttpHeaderValue("testK"), "testV");
    }

    @Test
    public void testGetUrl() {
        assertEquals(httpServerRequest.getUri(), URI.create("msf4j"));
    }

    @Test
    public void testGetHttpMethod() {
        assertEquals(httpServerRequest.getHttpMethod(), HttpMethod.GET);
    }
}
