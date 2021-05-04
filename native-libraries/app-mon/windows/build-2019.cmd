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
cd ..\..\..
set SAFEKEEPING_ROOT=%CD%
@if "%1" == "/clean" goto :CLEAN
@if "%1" == "/build" goto :BUILD
@if "%1" == "" goto :BUILD
@if "%1" == "/?" goto :HELP
goto :HELP

:BUILD
@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

call "%ProgramFiles(x86)%\Microsoft Visual Studio\%VS_VERSION%\Professional\VC\Auxiliary\Build\vcvars64.bat"

set JAVA_HOME=%SAFEKEEPING_ROOT%\jdk
echo set JAVA_HOME to %JAVA_HOME%  
cd "%APP_HOME%" 
if not exist release (mkdir release) 
nmake
copy release\VMGuestAppMonitorNative.dll %SAFEKEEPING_ROOT%\guest-app-monitor\src\main\resources
copy lib\vmware-appmonitor.dll %SAFEKEEPING_ROOT%\guest-app-monitor\src\main\resources
nmake clean
echo done
pause
popd
goto:eof

:CLEAN
echo clean resources
del /q %SAFEKEEPING_ROOT%\guest-app-monitor\src\main\resources\VMGuestAppMonitorNative.dll
del /q %SAFEKEEPING_ROOT%\guest-app-monitor\src\main\resources\vmware-appmonitor.dll
popd
echo done
goto:eof
:HELP
echo /build    Build VMGuestAppMonitorNative.dll [Default action]
echo /clean    Clean  %SAFEKEEPING_ROOT%\guest-app-monitor\src\main\resources directory
echo ?        This message
popd
