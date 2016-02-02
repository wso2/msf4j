#!/bin/bash
export METRICS_REPORTING_DAS_DATAAGENTCONFIGPATH=data-agent-conf.xml
export HTTP_MONITORING_DAS_DATAAGENTCONFIGPATH=data-agent-conf.xml
java -jar target/metrics-httpmon-fatjar*.jar

