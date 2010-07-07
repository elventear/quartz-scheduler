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

# Uncomment the following line if you would like to set log4j 
# logging properties
#
#LOGGING_PROPS="-Dlog4j.configuration=file:${workdir}/log4j.xml"

$JAVA -classpath $QUARTZ_CP $LOGGING_PROPS org.quartz.examples.example2.SimpleTriggerExample

