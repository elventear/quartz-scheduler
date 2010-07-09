@echo off
SET WORKDIR=%~d0%~p0

IF "%QUARTZ%"=="" SET QUARTZ=%WORKDIR%..\..

SET QEB=%QUARTZ%\examples\bin

SET TMP_CP=.

dir /b "%QUARTZ%\*.jar" 1 > temp.tmp 2> error.tmp
FOR /F %%I IN (temp.tmp) DO CALL "%QEB%\addpath.bat" "%QUARTZ%\%%I"

dir /b "%QUARTZ%\examples\*.jar" 1 > temp.tmp 2> error.tmp
FOR /F %%I IN (temp.tmp) DO CALL "%QEB%\addpath.bat" "%QUARTZ%\examples\%%I"

dir /b "%QUARTZ%\*.jar" 1 > temp.tmp 2> error.tmp
FOR /F %%I IN (temp.tmp) DO CALL "%QEB%\addpath.bat" "%QUARTZ%\build\%%I"

dir /b "%QUARTZ%\examples\*.jar" 1 > temp.tmp 2> error.tmp
FOR /F %%I IN (temp.tmp) DO CALL "%QEB%\addpath.bat" "%QUARTZ%\examples\build\%%I"

dir /b "%QUARTZ%\lib\*.jar" 1 > temp.tmp 2> error.tmp
FOR /F %%I IN (temp.tmp) DO CALL "%QEB%\addpath.bat" "%QUARTZ%\lib\%%I"

DEL error.tmp
DEL temp.tmp

IF NOT "%CLASSPATH%"=="" SET TMP_CP=%TMP_CP%;"%CLASSPATH%"


