@echo off

rem Set Quartz to the base directory of the Quartz Distribution
@SET QUARTZ=..\..

rem !!!!!!! Please read important information. !!!!!!
rem If "java" is not in your path, please set the path 
rem for Java 2 Runtime Environment in the path variable below
rem for example :
rem @SET PATH=D:\jdk1.3.1;%PATH%
rem 

@SET QUARTZ_CP=%QUARTZ%\lib\commons-beanutils.jar;%QUARTZ%\lib\commons-collections.jar;%QUARTZ%\lib\commons-digester.jar;%QUARTZ%\lib\commons-logging.jar;%QUARTZ%\lib\commons-dbcp-1.1.jar;%QUARTZ%\lib\commons-pool-1.1.jar;%QUARTZ%\lib\log4j-1.2.8.djar;%QUARTZ%\lib\jdbc2_0-stdext.jar;%QUARTZ%\lib\quartz.jar;%QUARTZ%\lib\examples.jar

rem Set LOG4J props if you are interested in setting up
rem a configuraiton file for log4j logging
rem @SET LOG4J_PROPS="-Dlog4j.configuration=log4j.properties"

rem Set the location and name of the quartz.properties file
@SET QUARTZ_PROPS="-Dorg.quartz.properties=instance1.properties"

rem Put the path to your JDBC driver(s) in this variable
@SET JDBC_CP=..\..\lib\postgres.jar
"java" -cp "%QUARTZ_CP%;%JDBC_CP%" %QUARTZ_PROPS% %LOG4J_PROPS% org.quartz.examples.example13.ClusterExample %1
