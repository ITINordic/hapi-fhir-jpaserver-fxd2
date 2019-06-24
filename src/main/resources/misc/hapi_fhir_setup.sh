#!/bin/bash

# Install java
sudo apt update
sudo add-apt-repository universe
sudo apt update

sudo apt install -y openjdk-8-jdk

# Add environmental varaiables and add path to JAVA_HOME
sudo tee /etc/environment <<EOF
JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64
EOF

source /etc/environment

echo $JAVA_HOME #(to check)

sudo apt install -y maven

sudo apt install -y postgresql-10


sudo sed -i "s/# listen_addresses = 'localhost'/listen_addresses = 'localhost'/g" /etc/postgresql/10/main/postgresql.conf

sudo systemctl restart postgresql

sleep 5

sudo adduser --system --no-create-home --disabled-login --group hapiuser

sleep 3

sudo -u postgres psql -c "create database fhirdb;"
sudo -u postgres psql -c "create user ${PGUSERNAME} with encrypted password '${PGPASSWD}';"
sudo -u postgres psql -c "grant all privileges on database fhirdb to ${PGUSERNAME};"

sudo groupadd tomcat

#Next, create a new tomcat user. We'll make this user a member of the tomcat group, with a home directory of /opt/tomcat (where we will install Tomcat), and with a shell of /bin/false (so nobody can log into the account):
sudo useradd -s /bin/false -g tomcat -d /opt/tomcat tomcat

cd /tmp

#Use curl to download the link that you copied from the Tomcat website:
curl -O https://www.apache.org/dist/tomcat/tomcat-9/v9.0.21/bin/apache-tomcat-9.0.21.tar.gz

#We will install Tomcat to the /opt/tomcat directory. Create the directory, then extract the archive to it with these commands:

sudo mkdir /opt/tomcat
sudo tar xzvf apache-tomcat-9.0.21.tar.gz -C /opt/tomcat --strip-components=1


#Now that the proper permissions are set up, we can create a systemd service file to manage the Tomcat process.
#               Create a systemd Service File
#We want to be able to run Tomcat as a service, so we will set up systemd service file.
#Tomcat needs to know where Java is installed. This path is commonly referred to as "JAVA_HOME". The easiest way to look up that location is by running this command:
sudo update-java-alternatives -l
#Output
#java-1.8.0-openjdk-amd64       1081       /usr/lib/jvm/java-1.8.0-openjdk-amd64
#Your JAVA_HOME is the output from the last column (highlighted in red). Given the example above, the correct JAVA_HOME for this server would be:
#JAVA_HOME
#/usr/lib/jvm/java-1.8.0-openjdk-amd64


#Paste the following contents into your service file. Modify the value of JAVA_HOME if necessary to match the value you found on your system. You may also want to modify the memory allocation settings that are specified in CATALINA_OPTS:
sudo tee /etc/systemd/system/tomcat.service <<EOF
[Unit]
Description=Apache Tomcat Web Application Container
After=network.target

[Service]
Type=forking

Environment=JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64
Environment=CATALINA_PID=/opt/tomcat/temp/tomcat.pid
Environment=CATALINA_HOME=/opt/tomcat
Environment=CATALINA_BASE=/opt/tomcat
Environment='CATALINA_OPTS=-Xms512M -Xmx1024M -server -XX:+UseParallelGC'
Environment='JAVA_OPTS=-Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom'

ExecStart=/opt/tomcat/bin/startup.sh
ExecStop=/opt/tomcat/bin/shutdown.sh

User=tomcat
Group=tomcat
UMask=0007
RestartSec=10
Restart=always

[Install]
WantedBy=multi-user.target
EOF

#Next, reload the systemd daemon so that it knows about our service file:
sudo systemctl daemon-reload

#Start the Tomcat service by typing:
sudo systemctl start tomcat
#Double check that it started without errors by typing:
sudo systemctl status tomcat

#                Clone hapi-fhir into folder of your choice
# 
# mkdir hapifhir
git clone https://github.com/ITINordic/hapi-fhir-jpaserver-rirmis.git
# 
# 
#     • Configure custom changes in hapi-fhir for the required database and dependencies (Changes dependant on version of hapi-fhir)
#     • (Current ones for hapi-fhir-jpaserver-starter on versioin 3.7.0)
# 
# 
# move to projet home folder(this case)
# 
cd ~/hapi-fhir-jpaserver-rirmis
# MANUAL INSTALL
# 
# open and add postgres dependency and properties in project
# 
# pom.xml
# src/main/resources/hapi.properties
# src/main/java/ca/uhn/fhir/jpa/starter/HapiProperties.java
# src/test/java/ca/uhn/fhir/jpa/starter/ExampleServerDstu2IT.java
# src/test/java/ca/uhn/fhir/jpa/starter/ExampleServerDstu3IT.java
# src/test/java/ca/uhn/fhir/jpa/starter/ExampleServerR4IT.java
# 
# 
# move back to the project diretory(hapi-fhir-jpaserver-starter), build and install the war
# 
mvn install
# if build and install are successful and it passes the database connetion tests, with the following as last part of the output:
# 
# ------------------------------------------------------------------------
# [INFO] BUILD SUCCESS
# [INFO] ------------------------------------------------------------------------
# [INFO] Total time: 50.884 s
# [INFO] Finished at: 2019-04-03T10:22:08+02:00
# [INFO] Final Memory: 24M/468M
# [INFO] ------------------------------------------------------------------------
# 
# Open the properties file again and edit the path to “lucenefiles” to point to your tomcat lucene path in the tomcat/bin folder.(prefarebly the full path)
# 
# 
# 
# MANUAL INSTALL
#open hapi.properties
#sudo vim hapi.propeties
#hibernate.search.default.indexBase=/opt/tomcat/bin/target/lucenefiles
# 
# navigate back to the project home folder and rebuild the war file.(this time without the tests)
# 
mkdir -p /opt/tomcat/bin/target/lucenefiles
# 
# you get the following as output if successful:
# ------------------------------------------------------------------------
# [INFO] BUILD SUCCESS
# [INFO] ------------------------------------------------------------------------
# [INFO] Total time: 6.412 s
# [INFO] Finished at: 2019-04-03T10:29:30+02:00
# [INFO] Final Memory: 25M/337M
# [INFO] ------------------------------------------------------------------------
# 
# navigate to the war folder in the projet home diretory
# 
cd target/
# 
# and copy or move the war file to the webapps folder of tomcat.
# 
sudo mv hapi-fhir-jpaserver.war /opt/tomcat/webapps/
# 
# reload the tomcat daemon activities, and restart tomcat
# 
#Next, we can set up the proper user permissions for our installation.

#               Update Permissions
#The tomcat user that we set up needs to have access to the Tomcat installation. We'll set that up now.
#Change to the directory where we unpacked the Tomcat installation:
cd /opt/tomcat
#Give the tomcat group ownership over the entire installation directory:
#Next, give the tomcat group read access to the conf directory and all of its contents, and execute access to the directory itself:
sudo chmod -R g+r conf
sudo chmod g+x conf
#Make the tomcat user the owner of the webapps, work, temp, and logs directories:
sudo chown -R tomcat:tomcat webapps/ work/ temp/ logs/ bin/ conf/

sudo systemctl restart tomcat
# 
# then access the server as:
# 
# http://localhost:8080/hapi-fhir-jpaserver
# 
# 