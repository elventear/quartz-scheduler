@echo off

rem All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.

setlocal

set TC_HOME=..\..\..

start "terracotta" "%TC_HOME%\bin\start-tc-server.bat"

endlocal