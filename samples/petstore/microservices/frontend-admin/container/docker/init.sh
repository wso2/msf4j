#!/bin/bash

env >> /etc/environment
echo ". /etc/environment" >> /etc/apache2/envvars
/etc/init.d/apache2 start && \
tail -F /var/log/apache2/*log

