#!/bin/bash

env >> /etc/environment
echo ". /etc/environment" >> /etc/apache2/envvars
/usr/sbin/apache2ctl -D FOREGROUND

