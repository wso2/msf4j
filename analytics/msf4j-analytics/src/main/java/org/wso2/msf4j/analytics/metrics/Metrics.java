/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.msf4j.analytics.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.msf4j.analytics.internal.DataHolder;

/**
 * A utility class to keep Metric Services.
 */
public final class Metrics {

    private static final Logger logger = LoggerFactory.getLogger(Metrics.class);

    private volatile MetricService metricService;

    private volatile MetricManagementService metricManagementService;

    private Metrics() {
    }

    /**
     * Initializes the Metrics instance
     */
    private static class MetricsHolder {
        private static final Metrics INSTANCE = new Metrics();
    }

    /**
     * This returns the Metrics singleton instance.
     *
     * @return The Metrics instance
     */
    public static Metrics getInstance() {
        return MetricsHolder.INSTANCE;
    }

    /**
     * Initialize metric services
     */
    private void initializeServices() {
        if (logger.isInfoEnabled()) {
            logger.info("Initializing Metrics Services");
        }
        org.wso2.carbon.metrics.core.Metrics metrics =
                new org.wso2.carbon.metrics.core.Metrics(DataHolder.getInstance().getConfigProvider());
        // Activate metrics
        metrics.activate();

        metricService = metrics.getMetricService();
        metricManagementService = metrics.getMetricManagementService();

        // Deactivate Metrics at shutdown
        Thread thread = new Thread(() -> metrics.deactivate());
        Runtime.getRuntime().addShutdownHook(thread);
    }

    /**
     * Returns the {@link MetricService}
     *
     * @return The {@link MetricService} instance
     */
    public MetricService getMetricService() {
        if (metricService == null) {
            synchronized (this) {
                if (metricService == null) {
                    initializeServices();
                }
            }
        }
        return metricService;
    }

    /**
     * Set the {@link MetricService} service
     *
     * @param metricService The {@link MetricService} reference
     */
    void setMetricService(MetricService metricService) {
        this.metricService = metricService;
    }

    /**
     * Returns the {@link MetricManagementService}
     *
     * @return The {@link MetricManagementService} instance
     */
    public MetricManagementService getMetricManagementService() {
        if (metricManagementService == null) {
            synchronized (this) {
                if (metricManagementService == null) {
                    initializeServices();
                }
            }
        }
        return metricManagementService;
    }

    /**
     * Set the {@link MetricManagementService} service
     *
     * @param metricManagementService The {@link MetricManagementService} reference
     */
    void setMetricManagementService(MetricManagementService metricManagementService) {
        this.metricManagementService = metricManagementService;
    }

}
