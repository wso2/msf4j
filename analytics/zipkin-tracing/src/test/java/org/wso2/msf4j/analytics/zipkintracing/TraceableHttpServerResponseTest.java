package org.wso2.msf4j.analytics.zipkintracing;

import com.github.kristofa.brave.http.HttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.msf4j.Response;

import java.io.IOException;

/**
 * Class for testing TraceableHttpServerResponse.
 */
public class TraceableHttpServerResponseTest extends Assert {

    private Response response;
    private HttpResponse httpResponse;

    @BeforeClass
    public void setUp() throws IOException {
        HTTPCarbonMessage httpCarbonMessage =
                new HTTPCarbonMessage(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
        response = new Response(httpCarbonMessage);
        response.setStatus(200);
        httpResponse = new TraceableHttpServerResponse(response);
    }

    @Test
    public void testGetStatusCode() {
        assertEquals(httpResponse.getHttpStatusCode(), response.getStatusCode());
    }

}
