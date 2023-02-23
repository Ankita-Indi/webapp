#!/bin/bash

# sudo cp -r ./uploads/ /home/ec2/
echo 'Updating Packages'
sudo yum update -y

echo 'cleaning cache'
sudo yum clean all
sudo yum makecache
echo 'installing Open jdk 8'
cd ~
sudo yum install wget -y
sleep 10
sudo yum install java-1.8.0-openjdk -y
sleep 10
java -version
echo 'installed java successfully.'


sudo wget https://dev.mysql.com/get/mysql80-community-release-el7-3.noarch.rpm
sudo rpm --import https://repo.mysql.com/RPM-GPG-KEY-mysql-2022
sudo rpm -Uvh mysql80-community-release-el7-3.noarch.rpm
sudo yum install mysql-server -y
echo "sleeping....."
sleep 10
# sudo systemctl start mysqld.service
# sudo systemctl status mysqld.service
echo "sleeping....."
sleep 10
# pwd = sudo grep -op 'temporary password(.*): \K(\S+)' /var/log/mysqld.log
sudo yum -y install mysql-community-server
sudo systemctl enable --now mysqld
systemctl status mysqld
passwords=$(sudo grep 'temporary password' /var/log/mysqld.log | awk {'print $13'})
sudo mysql --connect-expired-password -u root -p$passwords -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'Passme@1234'"
# sudo mysql --connect-expired-password -u root -p$pwd "Alter USER 'root'@'localhost' IDENTIFIED BY 'Pratappk@890'"
# pwd=`sudo grep 'temporary password' /var/log/mysqld.log | rev | cut -d':' -f 1 | rev | xargs`
# sudo mysql -uroot -p$pwd --connect-expired-password -e "Alter user 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'Pratappk@890'"
# sudo mysql -uroot -pPratappk@890  -e "CREATE DATABASE IF NOT EXISTS user_db"

pwd

sudo mkdir webservice
# sudo mv health-check-api-0.0.1-SNAPSHOT.jar webservice/
# sudo mv health-check-api.service /etc/systemd/system/health-check-api.service
pwd
sudo chmod 755 /home/ec2/health-check-api.service
sudo chmod 755 /home/ec2/health-check-api-0.0.1-SNAPSHOT.jar



# sudo mv health-check-api.service /etc/systemd/system/health-check-api.service

sudo systemctl start health-check-api.service
sudo systemctl enable health-check-api.service
sudo systemctl status health-check-api.service
