

QRTZ_CP=../lib/commons-logging.jar:../lib/commons-collections.jar:../lib/commons-dbcp-1.1.jar:../lib/commons-pool-1.1.jar:../lib/log4j.jar:../lib/jdbc2_0-stdext.jar:../lib/quartz.jar:../lib/examples.jar

JDBC_CP=../lib/oracle-1.2.jar


# you'll need to set this to the absolute path to your quartz.jar file...

RMI_CODEBASE=file:/home/jhouse/wrk/quartz-1.0.7/lib/quartz.jar


java -cp $QRTZ_CP:$JDBC_CP -Djava.rmi.server.codebase=$RMI_CODEBASE -Djava.security.policy=rmi.policy -Dorg.quartz.properties=quartzServer.properties org.quartz.impl.QuartzServer console
