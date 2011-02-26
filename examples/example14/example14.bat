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

"java" -cp %QUARTZ_CP% %LOG4J_PROPS% org.quartz.examples.example14.PriorityExample
