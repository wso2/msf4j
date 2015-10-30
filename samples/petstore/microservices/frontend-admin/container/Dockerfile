From ubuntu:14.04

RUN sudo apt-get update && apt-get install -y apache2 php5 php5-curl php5-redis

RUN sudo apt-get install -y openssh-server git vim 
RUN mkdir -p /var/run/sshd
RUN echo 'root:stratos' | chpasswd
RUN sed -i "s/PermitRootLogin without-password/#PermitRootLogin without-password/" /etc/ssh/sshd_config
EXPOSE 22

expose 80

RUN rm -fr /var/www/html/*
COPY html /var/www/html/
COPY php.ini /etc/php5/apache2/
COPY init.sh /opt/
RUN chmod 755 /opt/init.sh
ENTRYPOINT /opt/init.sh 
