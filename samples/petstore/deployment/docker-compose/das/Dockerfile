# ------------------------------------------------------------------------
#
# Copyright 2016 WSO2, Inc. (http://wso2.com)
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

# ------------------------------------------------------------------------

FROM wso2/wso2das:3.0.1
MAINTAINER architecture@wso2.org

RUN mkdir -p /mnt/wso2das-3.0.1/repository/deployment/server/carbonapps
COPY modules /mnt/wso2-artifacts/modules/
COPY repository /mnt/wso2-artifacts/repository/
#COPY packages/wso2das-msf4j-tracing-2.0.1-SNAPSHOT.car /mnt/wso2das-3.0.1/repository/deployment/server/carbonapps

ENTRYPOINT ["/usr/local/bin/init.sh"]
