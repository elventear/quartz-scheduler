@echo off

IF "%QUARTZ%"=="" SET QUARTZ=..\..

SET QEB=%QUARTZ%\examples\bin

SET TMP_CP=.

dir /b "%QUARTZ%\*.jar" > temp.tmp
FOR /F %%I IN (temp.tmp) DO CALL "%QEB%\addpath.bat" "%QUARTZ%\%%I"

dir /b "%QUARTZ%\*.jar" > temp.tmp
FOR /F %%I IN (temp.tmp) DO CALL "%QEB%\addpath.bat" "%QUARTZ%\build\%%I"

dir /b "%QUARTZ%\lib\*.jar" > temp.tmp
FOR /F %%I IN (temp.tmp) DO CALL "%QEB%\addpath.bat" "%QUARTZ%\lib\%%I"

DEL temp.tmp
