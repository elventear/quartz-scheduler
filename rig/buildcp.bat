@echo off

IF "%QUARTZ%"=="" SET QUARTZ=..

SET QEB=%QUARTZ%\rig

SET TMP_CP=.

dir /b "%QUARTZ%\*.jar" > temp.tmp
FOR /F %%I IN (temp.tmp) DO CALL "%QEB%\addpath.bat" "%QUARTZ%\%%I"

dir /b "%QUARTZ%\examples\*.jar" > temp.tmp
FOR /F %%I IN (temp.tmp) DO CALL "%QEB%\addpath.bat" "%QUARTZ%\examples\%%I"

dir /b "%QUARTZ%\*.jar" > temp.tmp
FOR /F %%I IN (temp.tmp) DO CALL "%QEB%\addpath.bat" "%QUARTZ%\build\%%I"

dir /b "%QUARTZ%\examples\*.jar" > temp.tmp
FOR /F %%I IN (temp.tmp) DO CALL "%QEB%\addpath.bat" "%QUARTZ%\examples\build\%%I"

dir /b "%QUARTZ%\lib\core\*.jar" > temp.tmp
FOR /F %%I IN (temp.tmp) DO CALL "%QEB%\addpath.bat" "%QUARTZ%\lib\core\%%I"

dir /b "%QUARTZ%\lib\build\*.jar" > temp.tmp
FOR /F %%I IN (temp.tmp) DO CALL "%QEB%\addpath.bat" "%QUARTZ%\lib\build\%%I"

dir /b "%QUARTZ%\lib\optional\*.jar" > temp.tmp
FOR /F %%I IN (temp.tmp) DO CALL "%QEB%\addpath.bat" "%QUARTZ%\lib\optional\%%I"

DEL temp.tmp

IF NOT "%CLASSPATH%"=="" SET TMP_CP=%TMP_CP%;"%CLASSPATH%"

