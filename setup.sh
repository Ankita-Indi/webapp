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

sudo chmod 544 /tmp/health-check-api.service
sudo mv /tmp/health-check-api.service /etc/systemd/system/health-check-api.service

