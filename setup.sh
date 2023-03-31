#!/bin/bash
echo 'Updating Packages'
sudo yum update -y
sleep 10
echo 'cleaning cache'
sudo yum clean all
sudo yum makecache
echo 'installing Open jdk 8'
cd ~
sudo yum install wget -y
echo "sleeping....."
sleep 10
sudo yum install java-1.8.0-openjdk -y
echo "sleeping....."
sleep 10
java -version
echo 'installed java successfully.'

sudo wget https://dev.mysql.com/get/mysql80-community-release-el7-3.noarch.rpm
sudo rpm --import https://repo.mysql.com/RPM-GPG-KEY-mysql-2022
sudo rpm -Uvh mysql80-community-release-el7-3.noarch.rpm
echo "sleeping....."
sleep 10
sudo yum install -y mysql-community-client
sleep 10
sudo yum install amazon-cloudwatch-agent -y

sudo chmod 544 /tmp/health-check-api.service
sudo chmod 774 /tmp/cloudwatchagent_config.json
sudo mv /tmp/health-check-api.service /etc/systemd/system/health-check-api.service
sudo mv /tmp/cloudwatch-config.json /opt/aws/amazon-cloudwatch-agent/cloudwatch-config.json
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/opt/aws/amazon-cloudwatch-agent/cloudwatch-config.json -s


pwd
sudo mkdir logs
sudo chown -R ec2-user logs
cd logs
pwd

