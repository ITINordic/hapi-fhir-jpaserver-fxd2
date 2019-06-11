#!/bin/bash

#set up dhis2-tools repositories
sudo add-apt-repository ppa:bobjolliffe/dhis2-tools
sudo apt-get update

#install dhis2-tools
sudo apt-get -y install dhis2-tools

#install postgres and postgis which is important for DHIS2 newer versions
sudo apt-get -y install postgresql-10 postgresql-contrib-10 postgresql-10-postgis-2.4

#install java (TODO: Ensure a single Java installation - (java -version))
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get install oracle-java8-installer
sudo apt-get install oracle-java8-set-default

#turn your user (eg bobj) into a dhis2-admin user by running:
export MYUSER=$(id -un)
sudo dhis2-create-admin $MYUSER

#create an instance named eg dhis with:
export MYINSTANCE=dhis
dhis2-instance-create $MYINSTANCE

#create db here if need be
sudo -u postgres createdb -O $MYUSER $MYINSTANCE

#setup postgresql for dhis2 gis
sudo -u postgres psql -c "create extension postgis;" $MYINSTANCE

#restore dhis2 database(Uncomment line below if needed)
#dhis2-restoredb $MYINSTANCE <path-to-database-location>

#deploy dhis2 war(Uncomment to install latest stable version)
#dhis2-deploy-war dhis

#deploy dhis2 war(Uncomment to install custom version)
#dhis2-deploy-war -i (manually provide link to preferred version)

#start your dhis instance with:
dhis2-startup $MYINSTANCE 

#view log if need be(Uncomment to view log)
#dhis2-logview

echo '** DHIS2 INSTALLATION COMPLETE **'















