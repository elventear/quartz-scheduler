@echo off

rem Set Quartz to the base directory of the Quartz Distribution
@SET QUARTZ=..\..

rem !!!!!!! Please read important information. !!!!!!
rem If "java" is not in your path, please set the path 
rem for Java 2 Runtime Environment in the path variable below
rem for example :
rem @SET PATH=D:\jdk1.3.1;%PATH%
rem 

@SET QUARTZ_CP=%QUARTZ%\lib\commons-collections.jar;%QUARTZ%\lib\commons-logging.jar;%QUARTZ%\lib\commons-dbcp-1.1.jar;%QUARTZ%\lib\commons-pool-1.1.jar;%QUARTZ%\lib\log4j-1.2.8.djar;%QUARTZ%\lib\jdbc2_0-stdext.jar;%QUARTZ%\lib\quartz.jar;%QUARTZ%\lib\examples.jar

rem Set LOG4J props if you are interested in setting up
rem a configuraiton file for log4j logging
rem @SET LOG4J_PROPS="-Dlog4j.configuration=log4j.properties"

"java" -cp "%QUARTZ_CP%" %LOG4J_PROPS% org.quartz.examples.example5.MisfireExample
