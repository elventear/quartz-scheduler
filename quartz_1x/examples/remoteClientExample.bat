@rem @echo off
@SET QRTZ=..


@rem !!!!!!! Please read important information. !!!!!!
@rem If "java" is not in your path, please set the path 
@rem for Java 2 Runtime Environment in the path variable below
@rem for example :
@rem   SET PATH=D:\jdk1.3.1;%PATH%
@rem 

@SET QRTZ_CP=%QRTZ%\lib\commons-collections.jar;%QRTZ%\lib\commons-dbcp-1.1.jar;%QRTZ%\lib\commons-pool-1.1.jar;%QRTZ%\lib\log4j.jar;%QRTZ%\lib\jdbc2_0-stdext.jar;%QRTZ%\lib\quartz.jar;%QRTZ%\lib\examples.jar

@rem Put the path to your JDBC driver(s) in this variable
@rem for example :
@rem   SET JDBC_CP=d:\repository\java\lib\oracle-1.2.jar
@SET JDBC_CP=d:\repository\java\lib\oracle-1.2.jar


"java" -cp "%QRTZ_CP%;%JDBC_CP%" -Dorg.quartz.properties=remoteClient.properties org.quartz.examples.QuartzRemoteClient
