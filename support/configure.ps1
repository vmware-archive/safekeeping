function Check-LoadedModule
{
  Param( [parameter(Mandatory = $true)][alias("Module")][string]$ModuleName,
 [Switch]$AllowClobber )
  $LoadedModules = Get-Module | Select Name
  if ($($LoadedModules -like "*$ModuleName*").count -eq 0) {Install-Module -Name $ModuleName -SkipPublisherCheck:$true -Confirm:$false -AllowClobber:$AllowClobber}
}


#Check-LoadedModule -ModuleName VSSetup
#Install-Module -Name Pscx -AllowClobber -Confirm:$false
#Check-LoadedModule -ModuleName Pscx -AllowClobber 
#Import-VisualStudioVars -VisualStudioVersion:2012 -Architecture x64
#Uninstall-Module -Name Pscx
#Uninstall-Module -Name VSSetup


if (-not (test-path "$env:ProgramFiles\7-Zip\7z.exe")) {throw "$env:ProgramFiles\7-Zip\7z.exe needed"} 
set-alias szip "$env:ProgramFiles\7-Zip\7z.exe"  

$JavaLibrary= dir ../packages/jdk-8u*|select FullName
$VddkLibrary= dir ../packages/VMware-vix-disklib-* |select FullName,Name

$VmbkVersion=Get-Content -Path "./VERSION"



$VddkExtFormatVersion=$($VddkLibrary.Name.Split('-'))[3]


$VmbkRepository=(Resolve-Path .\).Path 
$JdkDirectory="$VmbkRepository\vmbk\jdk"
$JdisklibDirectory="$VmbkRepository\jdisklib"
$LibBaseDirectory="$JdisklibDirectory\lib" 
$CCodeBaseDirectory="$JdisklibDirectory\src\"
$CIncludeBaseDirectory="$CCodeBaseDirectory\include" 

$AntHome="$VmbkRepository\apache-ant"

$VmbkJavaVersionFile="$VmbkRepository\vmbk\src\com\vmware\vmbk\control\VmbkVersion.java"


 

function Add-Java {
     Param([String] $JavaLibraryDirectory, [String]$JdkDirectory)
    write-host "Preparing Java Runtime"
    szip e $JavaLibraryDirectory .rsrc\1033\JAVA_CAB10
    Start-Process -Wait "C:\Windows\System32\extrac32.exe" -ArgumentList @("/y", "111")
    Remove-Item 111
    szip -o"$JdkDirectory" x tools.zip
    Remove-Item tools.zip 
    Get-ChildItem  -Recurse -Path "$JdkDirectory" -Filter *.pack | 
    Foreach-Object {
        &$JdkDirectory"\bin\unpack200" -r $_.FullName "$($_.DirectoryName)\$($_.BaseName).jar"
    }
    if (Test-Path $AntHome\jdk) { 
        Write-Host "$AntHome\jdk already exist"
    }
    else{
      #  New-Item -Path $AntHome\jdk -ItemType:Directory -Force
        Copy-Item  -path $JdkDirectory  -Destination $AntHome\jdk -Verbose -Force -Recurse
    }
}

function Add-Vddk{
    
     szip x -ovmware-vix-disklib-distrib $VddkLibrary.FullName 
    New-Item -Path $LibBaseDirectory -ItemType:Directory -Force
    Copy-Item  -path vmware-vix-disklib-distrib\lib\* -Destination $LibBaseDirectory -Verbose -Force
    Copy-Item  -path vmware-vix-disklib-distrib\include\* -Destination $CIncludeBaseDirectory -Verbose -Force
}

 

function Set-CodeVersion{
    $VddkExtFormatVersion=$($VddkLibrary.Name.Split('-'))[3]
    $INT_VERSION=$VddkExtFormatVersion.Split('.')
    $VddkMajorVersion =[int]$INT_VERSION[0]
    $VddkMinorVersion =[int] $INT_VERSION[1]
    $VddkPatchrVersion=[int]$INT_VERSION[2]
 
    Write-Host "VDDK VERSION: $VddkMajorVersion . $VddkMinorVersion . $VddkPatchrVersion" 
    New-Item -Path .\VDDK_VERSION -ItemType:File -Value $VddkExtFormatVersion -Force -Verbose
    $VERSION=$VddkMajorVersion*10000+$VddkMinorVersion*1000+10*$VddkPatchrVersion 
    Copy-Item  -path   $CCodeBaseDirectory\JDISKLIB_$VddkMajorVersion.$VddkMinorVersion.version  $CCodeBaseDirectory\jDiskLib.c -Force -Verbose
    Copy-Item  -path   $CIncludeBaseDirectory\JDISKLIBIMPL_$VddkMajorVersion.$VddkMinorVersion.version  $CIncludeBaseDirectory\jDiskLibImpl.h -Force -Verbose  

    cat $CIncludeBaseDirectory/VIX_DISK_LIB_VERSION.TEMPLATE |%{$_ -replace "XXXXXXXX","VERSION" -replace "YYYYYYYY","$VddkExtFormatVersion" } |Out-File $CIncludeBaseDirectory\vixDiskLibVersion.h -Verbose -Encoding ascii -Force
    cat $VmbkJavaVersionFile".template" |%{$_ -replace "XXXXXXXXXX","$VmbkVersion" -replace "VVVVVVVVVV","$VddkExtFormatVersion" } |Out-File $VmbkJavaVersionFile -Verbose -Encoding ascii -Force
	
	
    New-Item -Path $JdisklibDirectory\bin -ItemType:Directory -Force
    Copy-Item  -path "$JdisklibDirectory\prebuilt\win\$VddkMajorVersion.$VddkMinorVersion\*" -Destination $JdisklibDirectory\bin      -Force -Verbose  
    
}
 
szip x "-o$VmbkRepository"  "$VmbkRepository\support\apache-ant.zip"
Add-Java -JavaLibraryDirectory $JavaLibrary.FullName -JdkDirectory $JdkDirectory
Add-Vddk
Set-CodeVersion

Copy-Item  -path $CCodeBaseDirectory\Makefile-win -Destination $CCodeBaseDirectory\Makefile -Force -Verbose
Copy-Item  -path $JdisklibDirectory\Makefile-win -Destination $JdisklibDirectory\Makefile -Force -Verbose


$JavaHome="$AntHome\jdk"

[System.Environment]::SetEnvironmentVariable("ANT_HOME","$AntHome")
[System.Environment]::SetEnvironmentVariable("JAVA_HOME","$JavaHome")
[System.Environment]::SetEnvironmentVariable("PATH","$($env:Path);$JAVA_HOME\bin;$($env:ANT_HOME)\bin")

$enviromentString="@echo off`r`nset ANT_HOME=$AntHome`r`nset JAVA_HOME=$JavaHome`r`nset PATH=%PATH%;$JavaHome\bin;$AntHome\bin`r`n"
$enviromentString|Out-File $VmbkRepository\SetEnv.cmd -Encoding ascii -Force
