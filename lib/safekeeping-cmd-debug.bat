@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  safekeeping-cmd startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Set JAVA_HOME to the local JRE installation
set JAVA_HOME="%APP_HOME%\jre\win"
set PATH=%LOCALAPPDATA%\safekeeping\bin;%PATH%

@rem Add default JVM options here. You can also use JAVA_OPTS and SAFEKEEPING_CMD_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xms7G" "-Xmx30G" "-server" "-Xdebug" "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=1044"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\safekeeping-cmd-2.0.1.jar;%APP_HOME%\lib\safekeeping-core-ext-2.0.1.jar;%APP_HOME%\lib\jopt-simple-6.0.4.jar;%APP_HOME%\lib\safekeeping-core-2.0.1.jar;%APP_HOME%\lib\jvix-2.0.1.jar;%APP_HOME%\lib\safekeeping-common-2.0.1.jar;%APP_HOME%\lib\guava-29.0-jre.jar;%APP_HOME%\lib\jline-3.20.0.jar;%APP_HOME%\lib\jna-5.3.1.jar;%APP_HOME%\lib\jansi-2.0.1.jar;%APP_HOME%\lib\failureaccess-1.0.1.jar;%APP_HOME%\lib\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;%APP_HOME%\lib\jsr305-3.0.2.jar;%APP_HOME%\lib\checker-qual-2.11.1.jar;%APP_HOME%\lib\error_prone_annotations-2.3.4.jar;%APP_HOME%\lib\j2objc-annotations-1.3.jar;%APP_HOME%\lib\commons-math3-3.6.1.jar;%APP_HOME%\lib\aws-java-sdk-s3-1.11.1021.jar;%APP_HOME%\lib\aws-java-sdk-kms-1.11.1021.jar;%APP_HOME%\lib\aws-java-sdk-core-1.11.1021.jar;%APP_HOME%\lib\httpclient-4.5.13.jar;%APP_HOME%\lib\commons-codec-1.15.jar;%APP_HOME%\lib\jmespath-java-1.11.1021.jar;%APP_HOME%\lib\jackson-databind-2.11.4.jar;%APP_HOME%\lib\vim25.jar;%APP_HOME%\lib\pbm.jar;%APP_HOME%\lib\ssoclient.jar;%APP_HOME%\lib\vslm.jar;%APP_HOME%\lib\lookupservice-1.0.0.jar;%APP_HOME%\lib\vsphereautomation-client-sdk-3.5.0.jar;%APP_HOME%\lib\vapi-runtime-2.19.0.jar;%APP_HOME%\lib\vapi-authentication-2.19.0.jar;%APP_HOME%\lib\vapi-samltoken-2.19.0.jar;%APP_HOME%\lib\oidc-oauth2-sdk-0.0.1.jar;%APP_HOME%\lib\migz-1.0.4.jar;%APP_HOME%\lib\slf4j-jdk14-1.7.30.jar;%APP_HOME%\lib\jackson-annotations-2.11.4.jar;%APP_HOME%\lib\jackson-dataformat-cbor-2.6.7.jar;%APP_HOME%\lib\jackson-core-2.11.4.jar;%APP_HOME%\lib\commons-compress-1.20.jar;%APP_HOME%\lib\commons-lang-2.6.jar;%APP_HOME%\lib\concurrentli-1.2.0.jar;%APP_HOME%\lib\slf4j-api-1.7.30.jar;%APP_HOME%\lib\commons-logging-1.2.jar;%APP_HOME%\lib\ion-java-1.0.2.jar;%APP_HOME%\lib\joda-time-2.8.1.jar;%APP_HOME%\lib\httpcore-4.4.13.jar


@rem Execute safekeeping-cmd
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %SAFEKEEPING_CMD_OPTS%  -classpath "%CLASSPATH%" com.vmware.safekeeping.cmd.App %*

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable SAFEKEEPING_CMD_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%SAFEKEEPING_CMD_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
