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

$VddkDirectory="vddk"
$JvixResourcesDirectory="..\..\..\..\jvix\src\main\resources\"

function build([String] $Version ){

    $a=   $Version.split(".") 
    $Major=$a[0]
    $Minor=$a[1]
    $patch=$a[2]
    $Build=$a[3]
    $Version="$Major.$Minor"
 
    $ZipFile="VMware-vix-disklib-$Version.$Patch-$Build.x86_64.zip"

    echo "Checking $VMDK_DIRECTORY/$TAR_FILE file"
	if ( Test-Path   "$VddkDirectory/$ZipFile") {
    if (Test-Path "$VddkDirectory/vmware-vix-disklib-distrib"){
        Remove-Item -Path "$VddkDirectory/vmware-vix-disklib-distrib" -Force -Confirm:$false -Recurse
    }

     Expand-7Zip -ArchiveFileName "$VddkDirectory/$ZipFile" -TargetPath "$VddkDirectory/vmware-vix-disklib-distrib"  
     if (Test-Path "./$Version/bin"){
         Remove-Item -Path "./$Version/bin" -Force -Confirm:$false -Recurse
     }
     copy -Path "$VddkDirectory/vmware-vix-disklib-distrib/bin" -Destination "./$Version/bin" -Recurse  

     if (Test-Path "./$Version/lib"){
         Remove-Item -Path "./$Version/lib" -Force -Confirm:$false -Recurse
     }
     copy -Path "$VddkDirectory/vmware-vix-disklib-distrib/lib" -Destination "./$Version/lib" -Recurse   
     Remove-Item -Path "$VddkDirectory/vmware-vix-disklib-distrib" -Force -Confirm:$false -Recurse
     
     if (! (Test-Path "./$Version/release")){
         mkdir -Name "./$Version/release"  
     } 
     cd $Version
     $process="nmake"
     Start-Process $process   -Wait -NoNewWindow 
     copy -Path "./release/jDiskLib.dll" -Destination "./bin/"
     Compress-7Zip -ArchiveFileName "$Build.tar" -Format tar -Path "./bin" 
     echo "move -Path ""./$Build.tar"" -Destination ""$JvixResourcesDirectory\x64\$Major\$Minor\windows\$Patch""" 
     if (!(Test-Path "$JvixResourcesDirectory\x64\$Major\$Minor\windows\$Patch")){
        mkdir -Name "$JvixResourcesDirectory\x64\$Major\$Minor\windows\$Patch" 
     }
     if (Test-Path -Path "$JvixResourcesDirectory\x64\$Major\$Minor\windows\$Patch\$Build.tar"){
         Remove-Item -Path "$JvixResourcesDirectory\x64\$Major\$Minor\windows\$Patch\$Build.tar" -Force -Confirm:$false
     }
     
     move -Path "./$Build.tar" -Destination "$JvixResourcesDirectory\x64\$Major\$Minor\windows\$Patch" 
     Start-Process $process   -ArgumentList "clean"  -Wait -NoNewWindow  
     cd ..
     


   
     if (Test-Path "./$Version/lib"){
         Remove-Item -Path "./$Version/lib" -Force -Confirm:$false -Recurse
     }
     if (Test-Path "./$Version/bin"){
         Remove-Item -Path "./$Version/bin" -Force -Confirm:$false -Recurse
     }
     if (Test-Path "./$Version/release"){
       Remove-Item -Path "./$Version/release" -Force -Confirm:$false  -Recurse
     } 
        
    }

}



# See https://www.powershellgallery.com/ for module and version info
Function Install-ModuleIfNotInstalled(
    [string] [Parameter(Mandatory = $true)] $moduleName,
    [string] $minimalVersion
) {
    $module = Get-Module -Name $moduleName -ListAvailable |`
        Where-Object { $null -eq $minimalVersion -or $minimalVersion -ge $_.Version } |`
        Select-Object -Last 1
    if ($null -ne $module) {
         Write-Verbose ('Module {0} (v{1}) is available.' -f $moduleName, $module.Version)
    }
    else {
        Import-Module -Name 'PowershellGet'
        $installedModule = Get-InstalledModule -Name $moduleName -ErrorAction SilentlyContinue
        if ($null -ne $installedModule) {
            Write-Verbose ('Module [{0}] (v {1}) is installed.' -f $moduleName, $installedModule.Version)
        }
        if ($null -eq $installedModule -or ($null -ne $minimalVersion -and $installedModule.Version -lt $minimalVersion)) {
            Write-Verbose ('Module {0} min.vers {1}: not installed; check if nuget v2.8.5.201 or later is installed.' -f $moduleName, $minimalVersion)
            #First check if package provider NuGet is installed. Incase an older version is installed the required version is installed explicitly
            if ((Get-PackageProvider -Name NuGet -Force).Version -lt '2.8.5.201') {
                Write-Warning ('Module {0} min.vers {1}: Install nuget!' -f $moduleName, $minimalVersion)
                Install-PackageProvider -Name NuGet -MinimumVersion 2.8.5.201 -Scope CurrentUser -Force
            }        
            $optionalArgs = New-Object -TypeName Hashtable
            if ($null -ne $minimalVersion) {
                $optionalArgs['RequiredVersion'] = $minimalVersion
            }  
            Write-Warning ('Install module {0} (version [{1}]) within scope of the current user.' -f $moduleName, $minimalVersion)
            Install-Module -Name $moduleName @optionalArgs -Scope CurrentUser -Force -Verbose
        } 
    }
}

 
 Install-ModuleIfNotInstalled -moduleName "7Zip4Powershell"



if ($args.Count -eq 1) {
    build $args[0]
}else{
    $files = Get-ChildItem $VddkDirectory\*.zip
    if ($files.count -gt 0){
        foreach ($file in $files) {
            $x=$file.name
            $x -match "VMware-vix-disklib-(?<content>.*).x86_64.zip"   
            $Version=$matches['content'].Replace("-",".")
            build $Version  

        }
    }else {
     Write-Warning "no VDDK library inside $VddkDirectory directory"
    }
}
read-host “Press ENTER to continue...”

 #Remove-Item -path vddk -Recurse
