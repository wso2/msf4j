package org.wso2.msf4j.analytics.zipkintracing;

import com.github.kristofa.brave.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
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
        CarbonMessage carbonMessage = new DefaultCarbonMessage();
        response = new Response(carbonMessage1 -> {
        });
        response.setStatus(200);
        httpResponse = new TraceableHttpServerResponse(response);
    }

    @Test
    public void testGetStatusCode() {
        assertEquals(httpResponse.getHttpStatusCode(), response.getStatusCode());
    }

}
