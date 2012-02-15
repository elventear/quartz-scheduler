@echo off

SET WORKDIR=%~d0%~p0

IF "%QUARTZ%"=="" SET QUARTZ=%WORKDIR%..\..

SET QEB=%QUARTZ%\examples\bin

SET TMP_CP=.

dir /b "%QUARTZ%\examples\lib\*.jar" > temp.tmp
FOR /F %%I IN (temp.tmp) DO CALL "%QEB%\addpath.bat" "%QUARTZ%\examples\lib\%%I"

dir /b "%QUARTZ%\lib\*.jar" > temp.tmp
FOR /F %%I IN (temp.tmp) DO CALL "%QEB%\addpath.bat" "%QUARTZ%\lib\%%I"

DEL temp.tmp


