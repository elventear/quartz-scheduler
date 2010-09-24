@echo off

rem Set Quartz to the base directory of the Quartz Distribution
@SET QUARTZ=..\..

@rem setup the class path...
CALL ..\bin\buildcp.bat
SET QUARTZ_CP=%TMP_CP%

rem !!!!!!! Please read important information. !!!!!!
rem If "java" is not in your path, please set the path 
rem for Java 2 Runtime Environment in the path variable below
rem for example :
rem @SET PATH=D:\jdk1.3.1;%PATH%
rem 


rem Set LOG4J props if you are interested in setting up
rem a configuraiton file for log4j logging
rem @SET LOG4J_PROPS="-Dlog4j.configuration=log4j.properties"

rem Set the location and name of the quartz.properties file
@SET QUARTZ_PROPS="-Dorg.quartz.properties=instance1.properties"

rem Set the path to your Terracotta server home here
@SET TC_HOME=..\..\..

@SET TC_CP=%TC_HOME%/quartz/quartz-terracotta-1.2.0.jar;%TC_HOME%/common/terracotta-toolkit-1.0-runtime-1.0.0.jar

"java" -cp "%QUARTZ_CP%;%TC_CP%" %QUARTZ_PROPS% %LOG4J_PROPS% org.quartz.examples.example13.ClusterExample %1
