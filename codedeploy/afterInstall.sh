#!/bin/bash

# change ownership of the artifact
sudo chown ubuntu:ubuntu /home/ubuntu/webapp_jar/webapp-0.0.1-SNAPSHOT.jar

sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
    -a fetch-config \
    -m ec2 \
    -c file:/opt/cloudwatch-config.json \
    -s