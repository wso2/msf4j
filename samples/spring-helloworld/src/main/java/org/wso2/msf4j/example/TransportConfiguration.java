package org.wso2.msf4j.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wso2.msf4j.spring.transport.HTTPTransportConfig;

@Configuration
public class TransportConfiguration {

    @Bean
    public HTTPTransportConfig http() {
        return new HTTPTransportConfig(9090);
    }

}
