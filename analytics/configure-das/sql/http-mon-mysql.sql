-- 
-- Copyright 2015 WSO2 Inc. (http://wso2.org)
-- 
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--     http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- 

CREATE DATABASE IF NOT EXISTS mss_httpmon;

USE mss_httpmon;

CREATE TABLE IF NOT EXISTS REQUESTS_SUMMARY_PER_MINUTE 
(
  webappName            VARCHAR(100) NOT NULL,
  webappType            VARCHAR(15),
  serverName            VARCHAR(45),
  averageRequestCount   BIGINT,
  averageResponseTime   BIGINT,
  httpSuccessCount      BIGINT,
  httpErrorCount        BIGINT,
  sessionCount          BIGINT,
  TIME VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS HTTP_STATUS 
(
  webappName               VARCHAR(100) NOT NULL,
  serverName               VARCHAR(45),
  averageRequestCount      BIGINT,
  responseHttpStatusCode   INT,
  TIME VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS LANGUAGE 
(
  webappName            VARCHAR(100) NOT NULL,
  serverName            VARCHAR(45),
  averageRequestCount   BIGINT,
  LANGUAGE              VARCHAR(6),
  TIME VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS USER_AGENT_FAMILY 
(
  webappName            VARCHAR(100) NOT NULL,
  serverName            VARCHAR(45),
  averageRequestCount   BIGINT,
  userAgentFamily       VARCHAR(15),
  TIME VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS OPERATING_SYSTEM 
(
  webappName            VARCHAR(100) NOT NULL,
  serverName            VARCHAR(45),
  averageRequestCount   BIGINT,
  operatingSystem       VARCHAR(15),
  TIME VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS DEVICE_TYPE 
(
  webappName            VARCHAR(100) NOT NULL,
  serverName            VARCHAR(45),
  averageRequestCount   BIGINT,
  deviceCategory        VARCHAR(100),
  TIME VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS REFERRER 
(
  webappName            VARCHAR(100) NOT NULL,
  serverName            VARCHAR(45),
  averageRequestCount   BIGINT,
  referrer              VARCHAR(200),
  TIME VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS COUNTRY 
(
  webappName            VARCHAR(100) NOT NULL,
  serverName            VARCHAR(45),
  averageRequestCount   BIGINT,
  country               VARCHAR(200),
  TIME VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS WEBAPP_CONTEXT 
(
  webappName            VARCHAR(100) NOT NULL,
  serverName            VARCHAR(45),
  averageRequestCount   BIGINT,
  webappcontext         VARCHAR(200),
  TIME VARCHAR(100)
);
