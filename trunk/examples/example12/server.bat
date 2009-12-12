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
@SET QUARTZ_PROPS="-Dorg.quartz.properties=server.properties"
"java" -cp "%QUARTZ_CP%" %QUARTZ_PROPS% %LOG4J_PROPS% org.quartz.examples.example12.RemoteServerExample 
