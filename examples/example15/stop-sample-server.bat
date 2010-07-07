@echo off

rem All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.

setlocal

set TC_HOME=..\..\..

IF NOT EXIST "%TC_HOME%\bin\stop-tc-server.bat" (
echo "Please set TC_HOME properly."
exit /B
)

start "terracotta" "%TC_HOME%\bin\stop-tc-server.bat"

endlocal
