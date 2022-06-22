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

@if "%DEBUG%" == "" @echo off

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

@rem ##########################################################################
@rem
@rem  safekeeping powershell prepare.ps1 wrapper
@rem
@rem ##########################################################################
set PWSH7="%ProgramFiles%\PowerShell\7\pwsh.exe"
set PWSH6="%ProgramFiles%\PowerShell\6\pwsh.exe"
set PWSH="%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe"
set HELPMESSAGE=prepare.cmd -jdk ^<version^> ^| -Clean
if exist %PWSH6%.   set PWSH=%PWSH6%
if exist %PWSH7%.   set PWSH=%PWSH7% 

set argCount=0
for %%x in (%*) do (
   set /A argCount+=1
)

if %argCount% EQU 0 ( echo "%HELPMESSAGE%" ) else ( %PWSH%  -file "prepare.ps1" %*)

set PWSH7=
set PWSH6=
set PWSH=
