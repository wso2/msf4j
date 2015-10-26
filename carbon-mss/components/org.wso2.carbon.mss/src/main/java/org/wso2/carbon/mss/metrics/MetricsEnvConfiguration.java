package org.wso2.carbon.mss.metrics;

import org.wso2.carbon.metrics.common.MetricsConfiguration;

import java.util.Locale;

/**
 * Read Metrics Configurations from environment
 */
public class MetricsEnvConfiguration extends MetricsConfiguration {

    private static final String PREFIX = "METRICS_";

    public MetricsEnvConfiguration() {
    }

    @Override
    public String getFirstProperty(String key) {
        key = convertKey(key);
        String value = null;
        if (System.getProperty(key) != null) {
            value = System.getProperty(key);
        } else if (System.getenv(key) != null) {
            value = System.getenv(key);
        }
        return value;
    }

    private String convertKey(String key) {
        return PREFIX.concat(key.toUpperCase(Locale.getDefault()).replaceAll("\\.", "_"));
    }

}
