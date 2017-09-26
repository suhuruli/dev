@echo off
cd %~dp0
SET PATH=%cd%\lib;%PATH%
echo ------------------
echo %PATH%
echo ------------------
set port=%RANDOM:~-4%
#java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8880,suspend=y -Djava.library.path=%cd%\lib;$PATH -jar "rcmainservice.jar"
java -Djava.library.path=%cd%\lib;$PATH -jar "rcmainservice.jar"

set /p DUMMY=Hit ENTER to continue...