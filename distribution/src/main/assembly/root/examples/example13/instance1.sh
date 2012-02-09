#!/bin/sh

# Change this to your JDK installation root
#
#JAVA_HOME=/usr/java/jdk1.6.0_18

JRE=$JAVA_HOME/jre
JAVA=$JRE/bin/java

workdir=`dirname $0`
workdir=`cd ${workdir} && pwd`
QUARTZ=${workdir}/../..

. ${QUARTZ}/examples/bin/buildcp.sh

#
# Set the path to your JDCB Driver jar file here
# or just drop the jar in the Quartz lib folder
# JDBC_CP=/home/user/lib/postgres.jar

QUARTZ_CP=$QUARTZ_CP:$JDBC_CP

LOGGING_PROPS="-Dlog4j.configuration=file:${workdir}/log4j.xml"

# Set the name and location of the quartz.properties file
QUARTZ_PROPS="-Dorg.quartz.properties=${workdir}/instance1.properties"

$JAVA -classpath $QUARTZ_CP $QUARTZ_PROPS $LOGGING_PROPS org.quartz.examples.example13.ClusterExample $1

