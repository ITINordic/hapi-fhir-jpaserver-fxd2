#!/bin/bash

############### Setup dhis2-fhir-adapter ##########################

############## Create Database and User ##################################

#### Create a non-privileged user called dhis-fhir by invoking:
sudo -u postgres createuser -SDRP dhis-fhir
#### Enter dhis-fhir as password at the prompt. Create a database called dhis2-fhir by invoking:
sudo -u postgres createdb -O dhis-fhir dhis2-fhir
#### Add the uuid extension to the created database by invoking: 
sudo -u postgres psql dhis2-fhir
#### Enter the following command into the console:
CREATE EXTENSION "uuid-ossp";
#### Exit the console and return to your previous user with \q followed by exit.

 
############## Make Logs and Artemis Directories ##################################
sudo mkdir /etc/dhis-fhir/dhis/services/fhir-adapter/artemis
sudo mkdir /etc/dhis-fhir/dhis/services/fhir-adapter/logs
################ Alter permissions ##############################
sudo chmod -R 777 /etc/dhis-fhir/dhis/services/fhir-adapter/artemis
sudo chmod -R 777 /etc/dhis-fhir/dhis/services/fhir-adapter/logs

############## Download the new Adapter War File ##################################
cd /etc/dhis-fhir/dhis/services/fhir-adapter
sudo wget https://s3-eu-west-1.amazonaws.com/releases.dhis2.org/fhir/dhis2-fhir-adapter.war

sudo chmod -R 777 /etc/dhis-fhir/dhis/services/fhir-adapter/artemis
sudo chmod -R 777 /etc/dhis-fhir/dhis/services/fhir-adapter/logs


############## Run DHIS2 FHIR Adapter ###################################
export DHIS2_HOME="/etc/dhis-fhir/dhis";
nohup java -jar /etc/dhis-fhir/dhis/services/fhir-adapter/dhis2-fhir-adapter.war  > ~/dhis2-fhir-launch.log &
####### Verify DHIS2 FHIR Adapter is running ##################
curl http://localhost:8081/docs/api-guide.html