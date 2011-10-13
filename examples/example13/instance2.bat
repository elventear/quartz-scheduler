@echo off

rem Set Quartz to the base directory of the Quartz Distribution
@SET WD=%~d0%~p0
@SET QUARTZ=%WD%..\..

@rem setup the class path...
CALL "%WD%"..\bin\buildcp.bat
SET QUARTZ_CP=%TMP_CP%

rem !!!!!!! Please read important information. !!!!!!
rem If "java" is not in your path, please set the path 
rem for Java 2 Runtime Environment in the path variable below
rem for example :
rem @SET PATH=D:\jdk1.6.0_18;%PATH%
rem 

rem a configuration file for log4j logging
@SET LOG4J_PROPS="-Dlog4j.configuration=file:%WD%log4j.xml"

rem Set the location and name of the quartz.properties file
@SET QUARTZ_PROPS="-Dorg.quartz.properties=%WD%instance2.properties"

rem Put the path to your JDBC driver(s) in this variable
rem or just drop the jar in the "lib" folder
rem @SET JDBC_CP=..\..\lib\postgres.jar

"java" -cp "%QUARTZ_CP%;%JDBC_CP%" %QUARTZ_PROPS% %LOG4J_PROPS% org.quartz.examples.example13.ClusterExample dontScheduleJobs
