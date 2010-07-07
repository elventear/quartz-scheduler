#!/bin/sh

# Change this to your JDK installation root
#
#JAVA_HOME=/usr/java/j2sdk1.4.0_01

JRE=$JAVA_HOME/jre
JAVA=$JRE/bin/java

workdir=`dirname $0`
workdir=`cd ${workdir} && pwd`
QUARTZ=${workdir}/../..

. ${QUARTZ}/examples/bin/buildcp.sh

#
# Set the path to your JDCB Driver jar file here
JDBC_CP=/home/user/lib/postgres.jar

QUARTZ_CP=$QUARTZ_CP:$JDBC_CP

LOGGING_PROPS="-Dlog4j.configuration=file:${workdir}/log4j.xml"

# Set the name and location of the quartz.properties file
QUARTZ_PROPS="-Dorg.quartz.properties=instance1.properties"

$JAVA -classpath $QUARTZ_CP $QUARTZ_PROPS $LOGGING_PROPS org.quartz.examples.example13.ClusterExample $1

