@echo off
cls
 
FOR %%? IN ("%cd%") DO (
 set VMBK_DIR=%%~sf?
rem echo %VMBK_DIR%
)
set VMBK_LIB=%VMBK_DIR%\lib\
set OLDPATH=%PATH%
set PATH=%PATH%;%VMBK_DIR%\bin

set JAVA="%VMBK_DIR%\jre\bin\java"
%JAVA% -ea -server -Xms10G -Xmx10G -server -XX:NewSize=2G   -Djava.library.path=%VMBK_DIR%\bin -classpath %VMBK_LIB% -jar vmbk.jar %* 
set PATH=%OLDPATH%
set VMBK_DIR=
set VMBK_LIB=
set OLDPATH=
