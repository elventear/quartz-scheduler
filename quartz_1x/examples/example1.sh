#!/bin/sh

# You May Need To Change this to your Quartz installation root
QRTZ=..

# Change this to your JDK installation root
#
#JAVA_HOME=/usr/java/j2sdk1.4.0_01

JRE=$JAVA_HOME/jre
JAVA=$JRE/bin/java

QRTZ_CP=""
for f in $QRTZ/lib/*.jar; do
  QRTZ_CP=$QRTZ_CP:$f
done

echo $QRTZ_CP

$JAVA -classpath $QRTZ_CP -Dlog4jConfigFile=log4j.properties org.quartz.examples.SchedTest

