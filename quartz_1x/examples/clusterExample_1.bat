@echo off

@SET QRTZ=..

@echo .
@echo .  Read the contents of this batch file for info about this example!
@echo .
@echo .

rem
rem This is an example of using the clustering features of Quartz.  It requires
rem the use of JDBC-JobStore (and therefore a decent database).  Get comfortable
rem with the other examples before trying this one.
rem
rem You will need to edit the configuration properties for the cluster instances
rem ("cluster_inst1.properties" and "cluster_inst3.properties" ) for the the 
rem to set your database connection information.
rem
rem Pass the "clearJobs" argument to this batch file to get the scheduler to
rem clear all existing jobs..
rem
rem Read the JavaDOC (and the source code) for the ClusterTest.java file for 
rem more info about what this example does!
rem

rem
rem !!!!!!! Please read important information. !!!!!!
rem If "java" is not in your path, please set the path 
rem for Java 2 Runtime Environment in the path variable below
rem for example :
rem   SET PATH=D:\jdk1.3.1;%PATH%
rem 


@SET QRTZ_CP=%QRTZ%\lib\commons-collections.jar;%QRTZ%\lib\commons-logging.jar;%QRTZ%\lib\commons-dbcp-1.1.jar;%QRTZ%\lib\commons-pool-1.1.jar;%QRTZ%\lib\log4j.jar;%QRTZ%\lib\jdbc2_0-stdext.jar;%QRTZ%\lib\quartz.jar;%QRTZ%\lib\examples.jar

rem Put the path to your JDBC driver(s) in this variable
@SET JDBC_CP=..\lib\classes12.zip

"java" -cp "%QRTZ_CP%;%JDBC_CP%" -Dlog4jConfigFile=log4j.properties -Dorg.quartz.properties=cluster_inst1.properties org.quartz.examples.ClusterTest %1

