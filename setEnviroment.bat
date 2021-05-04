@echo off
set JAVA_HOME=%CD%\jdk
set JAXWS_HOME=%CD%\lib\wsgen 
set PATH=%JAVA_HOME%\bin;%JAXWS_HOME%;%PATH%
echo Enviroment configured
 