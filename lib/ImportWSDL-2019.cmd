@rem
@rem Copyright (C) 2020, VMware Inc
@rem All rights reserved.
@rem
@rem Redistribution and use in source and binary forms, with or without
@rem modification, are permitted provided that the following conditions are met:
@rem
@rem 1. Redistributions of source code must retain the above copyright notice,
@rem    this list of conditions and the following disclaimer.
@rem
@rem 2. Redistributions in binary form must reproduce the above copyright notice,
@rem    this list of conditions and the following disclaimer in the documentation
@rem    and/or other materials provided with the distribution.
@rem
@rem THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
@rem AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
@rem IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
@rem ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
@rem LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
@rem CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
@rem SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
@rem INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
@rem CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
@rem ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
@rem POSSIBILITY OF SUCH DAMAGE.
@rem 

@set VS_VERSION=2019
@if "%DEBUG%" == "" @echo off


pushd %CD%
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%.. 
@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi
cd "%APP_HOME%" 
cd ..
set SAFEKEEPING_ROOT=%CD%
@if "%1" == "/clean" goto :CLEAN
@if "%1" == "/import" goto :IMPORT
@if "%1" == "" goto :IMPORT
@if "%1" == "/?" goto :HELP
goto :HELP

:IMPORT
@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

call "%ProgramFiles(x86)%\Microsoft Visual Studio\%VS_VERSION%\Professional\VC\Auxiliary\Build\vcvars64.bat"

set JAVA_HOME=%SAFEKEEPING_ROOT%\jdk
set POWERSHELL=%SAFEKEEPING_ROOT%\PowerShell\Sapi
echo set JAVA_HOME to %JAVA_HOME%  
set WS_IMPORT="%JAVA_HOME%\bin\wsimport.exe"
set JAXWS_WS_IMPORT=%JAXWS_HOME%\wsimport.bat
set JAR= "%JAVA_HOME%\bin\jar.exe"
echo  Change dir to  %APP_HOME%   
cd "%APP_HOME%" 
IF EXIST  %WS_IMPORT% (
	echo JAVA distribution support WSGEN
    %WS_IMPORT%  -p com.vmware.sapi  -keep -clientjar "%APP_HOME%\sapi\sapi.jar" http://localhost:8080/sdk?wsdl 
) ELSE (
   call %JAXWS_WS_IMPORT% -p com.vmware.sapi  -keep -clientjar "%APP_HOME%\sapi\sapi.jar" http://localhost:8080/sdk?wsdl
)
rem call "%JAXWS_HOME%\wsimport.bat" -p com.vmware.sapi  -keep -clientjar .\sapi\sapi.jar http://localhost:8080/sdk?wsdl 
rem "c:\Program Files\Java\jdk1.8.0_261"\bin\wsimport -p com.vmware.sapi  -keep -clientjar .\sapi\sapi.jar http://localhost:8080/sdk?wsdl 
copy /Y .\META-INF\wsdl sapi\ 
del /q .\com\vmware\sapi\*.class
%JAR% -cvf "%APP_HOME%\sapi\sapi-src.jar" .\com
rmdir /q/s .\META-INF
rmdir /q/s .\com


echo import C# code
rem svcutil http://localhost:8080/sdk?wsdl /synconly /edb /language:c#
svcutil http://localhost:8080/sdk?wsdl /synconly  /language:c#
move "%APP_HOME%\SapiService.cs"  %POWERSHELL%\
move "%APP_HOME%\output.config"  %POWERSHELL%\
echo done
pause
popd
goto:eof

:CLEAN
echo clean resources
del /q "%APP_HOME%\sapi\sapi-src.jar"
del /q "%APP_HOME%\sapi\sapi.jar"
popd
echo done
goto:eof
:HELP
echo /import    Import WSGEN
echo /clean    Clean  %SAFEKEEPING_ROOT%\guest-app-monitor\src\main\resources directory
echo ?        This message
popd





@echo off
@rem cd C:\Users\mdaneri\Documents\safekeeping-gradle-2.0\safekeeping-soap-test\src\main\java
 set WS_IMPORT= "%JAVA_HOME%\bin\wsimport.exe"
 set JAXWS_WS_IMPORT=%JAXWS_HOME%\wsimport.bat
IF EXIST  %WS_IMPORT% (
	echo JAVA distribution support WSGEN
    %WS_IMPORT%  -p com.vmware.sapi  -keep -clientjar .\sapi\sapi.jar http://localhost:8080/sdk?wsdl 
) ELSE (
   call %JAXWS_WS_IMPORT% -p com.vmware.sapi  -keep -clientjar .\sapi\sapi.jar http://localhost:8080/sdk?wsdl
)
rem call "%JAXWS_HOME%\wsimport.bat" -p com.vmware.sapi  -keep -clientjar .\sapi\sapi.jar http://localhost:8080/sdk?wsdl 
rem "c:\Program Files\Java\jdk1.8.0_261"\bin\wsimport -p com.vmware.sapi  -keep -clientjar .\sapi\sapi.jar http://localhost:8080/sdk?wsdl 
copy /Y .\META-INF\wsdl sapi\ 
del /q .\com\vmware\sapi\*.class
jar -cvf sapi\sapi-src.jar .\com
rmdir /q/s .\META-INF
rmdir /q/s .\com

@rem call E:\jaxws-ri\bin\wsimport -p com.vmware.sapi  -clientjar C:\Users\mdaneri\Documents\safekeeping-gradle-2.0\lib\sapi.jar http://localhost:8080/Safekeeping/api?wsdl 
@rem createlink ln -s libvixDiskLib.so.7.0.1  libvixDiskLib.so.7.0.0
 