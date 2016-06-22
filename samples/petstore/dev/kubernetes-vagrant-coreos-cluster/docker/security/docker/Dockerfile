# ------------------------------------------------------------------------
#
# Copyright WSO2, Inc. (http://wso2.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License
#
# ------------------------------------------------------------------------

FROM java:8-jre
MAINTAINER architecture@wso2.org

WORKDIR /opt

# --------------------------------
# Copy scripts, packages & plugins
# --------------------------------
COPY packages/petstore-security.jar /opt/
COPY packages/client-truststore.jks /opt/
COPY packages/data-agent-conf.xml /opt/

# --------------------------------
# HTTP Monitoring and metrics specific config
# --------------------------------
ENV METRICS_REPORTING_DAS_DATAAGENTCONFIGPATH data-agent-conf.xml
ENV METRICS_REPORTING_DAS_RECEIVERURL "tcp://172.17.8.1:7612"
ENV HTTP_MONITORING_DAS_DATAAGENTCONFIGPATH data-agent-conf.xml
ENV HTTP_MONITORING_DAS_RECEIVERURL "tcp://172.17.8.1:7612"

# ----------------------
# Expose container ports
# ----------------------
EXPOSE 8080

ENTRYPOINT java -jar /opt/petstore-security.jar
