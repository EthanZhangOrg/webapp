#!/bin/bash

# start the application
touch /home/ubuntu/webapp_jar/webapp_log.log
sudo nohup java -jar /home/ubuntu/webapp_jar/webapp-0.0.1-SNAPSHOT.jar --server.port=80 --spring.config.location=/home/ubuntu/webapp_jar/application.properties > /home/ubuntu/webapp_jar/webapp_log.log 2>&1 &
echo "start application!"
