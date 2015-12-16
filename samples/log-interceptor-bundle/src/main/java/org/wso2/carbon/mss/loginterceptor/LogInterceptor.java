package org.wso2.carbon.mss.loginterceptor;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.mss.HandlerInfo;
import org.wso2.carbon.mss.HttpResponder;
import org.wso2.carbon.mss.Interceptor;

import java.util.Iterator;
import java.util.Map;

/**
 * Sample Interceptor which logs HTTP headers of the request.
 */
@Component(
        name = "org.wso2.carbon.mss.loginterceptor.LogInterceptor",
        service = Interceptor.class,
        immediate = true
)
public class LogInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(LogInterceptor.class);

    @Override
    public boolean preCall(HttpRequest request, HttpResponder responder, HandlerInfo handlerInfo) {
        Iterator<Map.Entry<String, String>> itr = request.headers().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, String> entry = itr.next();
            log.info("Header Name: " + entry.getKey() + " value : " + entry.getValue());
        }
        return true;
    }

    @Override
    public void postCall(HttpRequest request, HttpResponseStatus status, HandlerInfo handlerInfo) {

    }
}
