package org.wso2.msf4j.analytics.zipkintracing;

import com.github.kristofa.brave.http.HttpResponse;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.params.HttpParams;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.msf4j.delegates.client.MSF4JClientResponseContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import javax.ws.rs.client.ClientResponseContext;

/**
 * Class for testing TraceableHttpClientResponse.
 */
public class TraceableHttpClientResponseTest extends Assert {

    private ClientResponseContext clientResponseContext;
    private HttpResponse httpResponse;

    @BeforeClass
    public void setUp() throws IOException {
        clientResponseContext = new MSF4JClientResponseContext(new CloseableHttpResponse() {

            @Override
            public ProtocolVersion getProtocolVersion() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean containsHeader(String s) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Header[] getHeaders(String s) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Header getFirstHeader(String s) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Header getLastHeader(String s) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Header[] getAllHeaders() {
                return new Header[0];
            }

            @Override
            public void addHeader(Header header) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addHeader(String s, String s1) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setHeader(Header header) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setHeader(String s, String s1) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setHeaders(Header[] headers) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void removeHeader(Header header) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void removeHeaders(String s) {
                throw new UnsupportedOperationException();
            }

            @Override
            public HeaderIterator headerIterator() {
                throw new UnsupportedOperationException();
            }

            @Override
            public HeaderIterator headerIterator(String s) {
                throw new UnsupportedOperationException();
            }

            @Override
            public HttpParams getParams() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setParams(HttpParams httpParams) {
                throw new UnsupportedOperationException();
            }

            @Override
            public StatusLine getStatusLine() {
                return new StatusLine() {
                    @Override
                    public ProtocolVersion getProtocolVersion() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public int getStatusCode() {
                        return 0;
                    }

                    @Override
                    public String getReasonPhrase() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            @Override
            public void setStatusLine(StatusLine statusLine) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setStatusLine(ProtocolVersion protocolVersion, int i) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setStatusLine(ProtocolVersion protocolVersion, int i, String s) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setStatusCode(int i) throws IllegalStateException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setReasonPhrase(String s) throws IllegalStateException {
                throw new UnsupportedOperationException();
            }

            @Override
            public HttpEntity getEntity() {
                return new HttpEntity() {
                    @Override
                    public boolean isRepeatable() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public boolean isChunked() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public long getContentLength() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public Header getContentType() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public Header getContentEncoding() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public InputStream getContent() throws IOException, UnsupportedOperationException {
                        return null;
                    }

                    @Override
                    public void writeTo(OutputStream outputStream) throws IOException {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public boolean isStreaming() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public void consumeContent() throws IOException {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            @Override
            public void setEntity(HttpEntity httpEntity) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Locale getLocale() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setLocale(Locale locale) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void close() throws IOException {
                throw new UnsupportedOperationException();
            }
        });
        clientResponseContext.setStatus(200);
        httpResponse = new TraceableHttpClientResponse(clientResponseContext);
    }

    @Test
    public void testGetStatusCode() {
        assertEquals(httpResponse.getHttpStatusCode(), clientResponseContext.getStatus());
    }

}
