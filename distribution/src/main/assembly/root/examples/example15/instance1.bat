@echo off
setlocal
if not exist "%JAVA_HOME%\bin\java.exe" (
  echo "Please set JAVA_HOME"
  exit /b 1
)

rem Set Quartz to the base directory of the Quartz Distribution
@SET WD=%~d0%~p0
@SET QUARTZ=%WD%..\..

@rem setup the class path...
CALL "%WD%..\bin\buildcp.bat"
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
@SET QUARTZ_PROPS="-Dorg.quartz.properties=%WD%instance1.properties"

rem Set the path to your Terracotta server home here
@SET TC_HOME=%WD%..\..\..

IF NOT EXIST "%TC_HOME%\bin\start-tc-server.bat" (
echo "Modify the script to set TC_HOME"
exit /B
)

dir /b "%TC_HOME%\common\terracotta-toolkit*.jar" > temp.tmp
FOR /F %%I IN (temp.tmp) DO SET TC_CP="%TC_HOME%\common\%%I";%TC_CP%
del temp.tmp

dir /b "%TC_HOME%\quartz\quartz-terracotta*.jar" > temp.tmp
FOR /F %%I IN (temp.tmp) DO SET TC_CP="%TC_HOME%\quartz\%%I";%TC_CP%
del temp.tmp

"%JAVA_HOME%\bin\java.exe" -cp %QUARTZ_CP%;%TC_CP% %QUARTZ_PROPS% %LOG4J_PROPS% -Dtc.install-root=%TC_HOME% org.quartz.examples.example15.ClusterExample %1
endlocal