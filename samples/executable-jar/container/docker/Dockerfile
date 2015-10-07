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

FROM debian:7.7
MAINTAINER architecture@wso2.org

ENV DEBIAN_FRONTEND noninteractive

WORKDIR /opt

# --------------------------------
# Copy scripts, packages & plugins
# --------------------------------
COPY packages/SimpleStockQuote-1.0.0-SNAPSHOT.jar /opt/
ADD packages/jdk-8u60-linux-x64.gz /opt/

ENV JAVA_HOME /opt/jdk1.8.0_60

# ----------------------
# Expose container ports
# ----------------------
EXPOSE 8080

RUN apt-get update && apt-get install -y openssh-server
RUN mkdir -p /var/run/sshd
RUN echo 'root:wso2' | chpasswd
RUN sed -i "s/PermitRootLogin without-password/#PermitRootLogin without-password/" /etc/ssh/sshd_config
EXPOSE 22

ENTRYPOINT ${JAVA_HOME}/bin/java -jar /opt/SimpleStockQuote-1.0.0-SNAPSHOT.jar
