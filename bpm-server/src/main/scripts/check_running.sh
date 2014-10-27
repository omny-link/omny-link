#!/bin/bash
service=tomcat7

if (( $(ps -ef | grep -v grep | grep $service | wc -l) > 0 ))
then
  echo "$service is running!!!"
else
  echo `date`+'ERROR Had to restart server' >> /var/log/tomcat7/catalina.out 
  /etc/init.d/$service start
fi
