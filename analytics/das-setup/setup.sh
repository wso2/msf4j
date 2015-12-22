#!/bin/bash
# Copyright 2015 WSO2 Inc. (http://wso2.org)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# ----------------------------------------------------------------------------
# A script for setting up DAS
# ----------------------------------------------------------------------------

set -e

config_dir=$(dirname "$0")

das_dir=""
function help {
    echo ""
    echo "Usage: "
    echo "setup.sh -d <DAS_HOME>"
    echo ""
    echo "-d: WSO2 Data Analytics Server (DAS) home directory"
    echo ""
}

checkcommand () {
    command -v $1 >/dev/null 2>&1 || { echo >&2 "Command $1 not found."; exit 1; }
}

while getopts "d:" opts
do
  case $opts in
    d)
        das_dir=${OPTARG}
        ;;
    \?)
        help
        exit 1
        ;;
  esac
done

if [[ ! -d $das_dir ]]; then
    echo "Please specify the WSO2 DAS Home."
    help
    exit 1
fi

checkcommand unzip

echo "Copying Carbon Apps to DAS."
mkdir -p $das_dir/repository/deployment/server/carbonapps
cp $config_dir/capps/*.car $das_dir/repository/deployment/server/carbonapps

echo "Setting up HTTP Monitoring Dashboard."
if [[ ! -d $das_dir/repository/deployment/server/jaggeryapps/monitoring ]]; then
    mkdir -p $das_dir/repository/deployment/server/jaggeryapps/monitoring
    unzip -q $config_dir/httpmon-dashboard/monitoring.zip -d $das_dir/repository/deployment/server/jaggeryapps/monitoring
fi
unzip -q -o $config_dir/httpmon-dashboard/modules.zip -d $das_dir/modules/

echo "Completed..."
