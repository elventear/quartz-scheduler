

QRTZ_CP=../lib/commons-logging.jar:../lib/commons-collections.jar:../lib/commons-dbcp-1.1.jar:../lib/commons-pool-1.1.jar:../lib/log4j.jar:../lib/jdbc2_0-stdext.jar:../lib/quartz.jar:../lib/examples.jar


java -cp $QRTZ_CP -Dorg.quartz.properties=remoteClient.properties org.quartz.examples.QuartzRemoteClient
