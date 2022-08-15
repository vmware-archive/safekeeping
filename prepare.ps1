 
<#
.Description
Configure Safekeeping Dev enviroment
.PARAMETER Jdk
Specify the JAVA version to use to build and distribuite Safekeeping
.PARAMETER Clean
Cleans and remove any Java JDK library file
.EXAMPLE
PS> .\Prepare.ps1 -Jdk 11
Configure Safekeeping to be built with JDK version 11
.EXAMPLE
PS> .\Prepare.ps1 -Clean 
Clean from any JDK file
.SYNOPSIS
Used to configure Safekeeping development enviroment
#>
param ( 
    [Int] [ValidateRange(8,11)] [Parameter  (Mandatory=$True,Position=0, HelpMessage="Java version to use ",ParameterSetName="Java")]  # Specify the JAVA version to use to build and distribuite Safekeeping 
    $Jdk , 
    [Parameter(Mandatory=$True,ParameterSetName="Clean")] [switch]$Clean 
    ) 

Function DeGZip-File{
    Param(
        $infile,
        $outfile = ($infile -replace '\.gz$','')
        )
    $input = New-Object System.IO.FileStream $inFile, ([IO.FileMode]::Open), ([IO.FileAccess]::Read), ([IO.FileShare]::Read)
    $output = New-Object System.IO.FileStream $outFile, ([IO.FileMode]::Create), ([IO.FileAccess]::Write), ([IO.FileShare]::None)
    $gzipStream = New-Object System.IO.Compression.GzipStream $input, ([IO.Compression.CompressionMode]::Decompress)
    $buffer = New-Object byte[](1024)
    while($true){
        $read = $gzipstream.Read($buffer, 0, 1024)
        if ($read -le 0){break}
        $output.Write($buffer, 0, $read)
        }
    $gzipStream.Close()
    $output.Close()
    $input.Close()
}

# See https://www.powershellgallery.com/ for module and version info
Function Install-ModuleIfNotInstalled(
    [string] [Parameter(Mandatory = $true)] $moduleName,
    [string] $minimalVersion
) {
    $module = Get-Module -Name $moduleName -ListAvailable |`
        Where-Object { $null -eq $minimalVersion -or $minimalVersion -ge $_.Version } |`
        Select-Object -Last 1
    if ($module) {
         Write-Verbose ('Module {0} (v{1}) is available.' -f $moduleName, $module.Version)
    }
    else {
        Import-Module -Name 'PowershellGet'
        $installedModule = Get-InstalledModule -Name $moduleName -ErrorAction SilentlyContinue
        if ($installedModule) {
            Write-Verbose ('Module [{0}] (v {1}) is installed.' -f $moduleName, $installedModule.Version)
        }
        if (!$installedModule -or ($minimalVersion -and $installedModule.Version -lt $minimalVersion)) {
            Write-Verbose ('Module {0} min.vers {1}: not installed; check if nuget v2.8.5.201 or later is installed.' -f $moduleName, $minimalVersion)
            #First check if package provider NuGet is installed. Incase an older version is installed the required version is installed explicitly
            if ((Get-PackageProvider -Name NuGet -Force).Version -lt '2.8.5.201') {
                Write-Warning ('Module {0} min.vers {1}: Install nuget!' -f $moduleName, $minimalVersion)
                Install-PackageProvider -Name NuGet -MinimumVersion 2.8.5.201 -Scope CurrentUser -Force
            }        
            $optionalArgs = New-Object -TypeName Hashta
            ble
            if ($minimalVersion) {
                $optionalArgs['RequiredVersion'] = $minimalVersion
            }  
            Write-Warning ('Install module {0} (version [{1}]) within scope of the current user.' -f $moduleName, $minimalVersion)
            Install-Module -Name $moduleName @optionalArgs -Scope CurrentUser -Force -Verbose
        } 
    }
} 

    

Function Define-Variable() {

   $Global:CurrentDirectory=(Get-Location).path
    $Global:jdkDir="$CurrentDirectory\jdk" 
    $Global:jdkDistribDir="$CurrentDirectory\jdk\dist" 

    $Global:JavaHome="$CurrentDirectory\jdk".Replace("\","/")
    $Global:javaDistribution="$JavaHome/dist"


    $Global:cxfDist="$CurrentDirectory\safekeeping-cxf\src\dist"
    $Global:cmdDist="$CurrentDirectory\safekeeping-cmd\src\dist"

    $Global:cxfJre="$cxfDist\jre"
    $Global:cmdJre="$cmdDist\jre"

    $Global:JreWindows="$jdkDistribDir\jre\win"
    $Global:jreLinux="$jdkDistribDir\jre\linux"
}

Function CleanUp() {
 
    Write-host "Starting CleanUp"
 
     if ( Test-Path -path $jdkDir) {
        Remove-Item   -Path $jdkDir -Recurse
    }
     if ( Test-Path -path "$CurrentDirectory\gradle.properties") {
        Remove-Item   -Path "$CurrentDirectory\gradle.properties"
    }

     if ( Test-Path -path "$CurrentDirectory\build.gradle") {
        Remove-Item   -Path "$CurrentDirectory\build.gradle"
    } 
  
    if (Test-Path -Path "$cxfJre"){
             Remove-Item -Path "$cxfJre" -Force -Confirm:$false -Recurse
      }

    if (Test-Path -Path "$cmdJre"){
             Remove-Item -Path "$cmdJre" -Force -Confirm:$false -Recurse  
    }

}



    if ($Clean){ 
        Define-Variable
        cleanUp 
        Write-Host  Done
        exit
    } 
switch($Jdk){
    8 { 
        $JdkVersion="VERSION_1_8"
        $ver="8u345"
        $rev="01"
        $VR=$ver+"b"+$rev
        $jdkFolder="jdk$ver-b$rev" 
        $openJdkUrlPath="https://github.com/adoptium/temurin8-binaries/releases/download/jdk$ver-b$rev"
        $jreWinFileName="OpenJDK8U-jre_x64_windows_hotspot_"+$VR+".zip"
        $jreLinuxFileName="OpenJDK8U-jre_x64_linux_hotspot_"+$VR+".tar.gz"
        $jdkWinFileName="OpenJDK8U-jdk_x64_windows_hotspot_"+$VR+".zip"
        $jdkLinuxFileName="OpenJDK8U-jdk_x64_linux_hotspot_"+$VR+".tar.gz"
    }
   11 {
        $JdkVersion="VERSION_11" 
        $ver="11.0.16"
        $rev="8"
        $VR=$ver+"_"+$rev
        $jdkFolder="jdk-$ver+$rev"
        $openJdkUrlPath="https://github.com/adoptium/temurin11-binaries/releases/download/jdk-$ver%2B$rev"
        $jreWinFileName="OpenJDK11U-jre_x64_windows_hotspot_"+$VR+".zip"
        $jreLinuxFileName="OpenJDK11U-jre_x64_linux_hotspot_"+$VR+".tar.gz"
        $jdkWinFileName="OpenJDK11U-jdk_x64_windows_hotspot_"+$VR+".zip"
        $jdkLinuxFileName="OpenJDK11U-jdk_x64_linux_hotspot_"+$VR+".tar.gz" 
    }
    

}
 
Write-Host "Configure Safekeeping to use Java version :$JdkVersion"
 
 
Install-ModuleIfNotInstalled -moduleName "7Zip4Powershell"



Define-Variable
CleanUp

[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
$client = New-Object System.Net.WebClient

#download JDK
New-Item -ItemType directory -Path $jdkDir 
Write-host  "Invoke-WebRequest -Uri $openJdkUrlPath/$jdkWinFileName -OutFile ""$jdkDir\$jdkWinFileName"""   
#Invoke-WebRequest -Uri "$openJdkUrlPath/$jdkWinFileName" -OutFile "$jdkDir\$jdkWinFileName"   
$client.DownloadFile("$openJdkUrlPath/$jdkWinFileName" , "$jdkDir\$jdkWinFileName" )
if (!(Test-Path "$jdkDir\$jdkWinFileName"))
{
    Write-Output "No $jdkDir\$jdkWinFileName file"
    exit
}
Expand-7Zip -ArchiveFileName "$jdkDir\$jdkWinFileName" -TargetPath $jdkDir 
Remove-Item  -Path "$jdkDir\$jdkWinFileName"
Get-ChildItem -Path "$jdkDir\$jdkFolder" | Move-Item -Destination $jdkDir
Remove-Item  -Path "$jdkDir\$jdkFolder"


  

New-Item -ItemType directory -Path $jdkDistribDir
New-Item -ItemType directory  -Path $jreLinux
New-Item -ItemType directory  -Path $JreWindows
 
#Windows distribution 
#Invoke-WebRequest -Uri  "$openJdkUrlPath/$jreWinFileName" -OutFile "$jdkDistribDir\$jreWinFileName"
$client.DownloadFile( "$openJdkUrlPath/$jreWinFileName","$jdkDistribDir\$jreWinFileName")

Expand-7Zip -ArchiveFileName "$jdkDistribDir\$jreWinFileName" -TargetPath  $JreWindows
$jrewin=(dir  "$JreWindows")[0]
cd -Path $jrewin.FullName
Move-Item -Path "*" -Destination ..
cd $CurrentDirectory 
Remove-Item -Path $jrewin.FullName
Remove-Item -Path "$jdkDistribDir\$jreWinFileName" 

#Linux distribution  
#Invoke-WebRequest -Uri "$openJdkUrlPath/$jreLinuxFileName" -OutFile "$jdkDistribDir\$jreLinuxFileName"
$client.DownloadFile("$openJdkUrlPath/$jreLinuxFileName" ,"$jdkDistribDir\$jreLinuxFileName")
DeGZip-File -infile   "$jdkDistribDir\$jreLinuxFileName" -outfile "$jreLinux\tarfile"

Expand-7Zip -ArchiveFileName "$jreLinux\tarfile" -TargetPath  "$jreLinux"

$jrelin=(dir  "$jreLinux")[0]
cd -Path $jrelin.FullName
Move-Item -Path "*" -Destination ..
cd $CurrentDirectory 
Remove-Item -Path $jrelin.FullName
Remove-Item -Path "$jreLinux\tarfile"
Remove-Item -Path "$jdkDistribDir\$jreLinuxFileName"
#end Distribution


Copy-Item -Path "$jdkDistribDir\jre"  -Destination  "$cxfJre" -Recurse
Copy-Item -Path "$jdkDistribDir\jre"  -Destination  "$cmdJre" -Recurse


$GradlePorperties=
"org.gradle.java.home=$JavaHome`n"+
"org.gradle.caching=true`n"+
"org.gradle.vfs.watch=true`n"+
"openJdkUrlPath=$openJdkUrlPath`n"+
"jreWinFileName=$jreWinFileName`n"+
"jreLinuxFileName=$jreLinuxFileName`n"+ 
"jdkWinFileName=$jdkWinFileName`n"+ 
"jdkLinuxFileName=$jdkLinuxFileName`n"+  
"javaDistribution=$javaDistribution`n"


set-content -Path "$CurrentDirectory\gradle.properties" -Value $GradlePorperties


$BuildGradle=
@'
/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java project to get you started.
 * For more details take a look at the Java Quickstart chapter in the Gradle
 * User Manual available at https://docs.gradle.org/6.5.1/userguide/tutorial_java_projects.html
 */
 
subprojects {
    apply plugin: 'eclipse'
    apply plugin: 'java' 
    java {
'@ +
"`n"+
"         sourceCompatibility = JavaVersion.$JdkVersion`n"+
"         targetCompatibility = JavaVersion.$JdkVersion`n"+
@'
    }
}


task info {
    doLast {
       println  "Root project:   ${project.rootProject}";
       println  "  rootDir:      ${project.rootDir}"
       println  "  projectDir:   ${project.projectDir}";
       println  "  project dir:  ${System.getProperty("user.dir")}";
    }
}
'@

set-content -Path "$CurrentDirectory\build.gradle" -Value $BuildGradle



Write-Output "Done - Use gradlew installDist to build Safekeeping"
 


#
# Copyright (C) 2020, VMware Inc
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# 1. Redistributions of source code must retain the above copyright notice,
#    this list of conditions and the following disclaimer.
#
# 2. Redistributions in binary form must reproduce the above copyright notice,
#    this list of conditions and the following disclaimer in the documentation
#    and/or other materials provided with the distribution.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.
# 
 