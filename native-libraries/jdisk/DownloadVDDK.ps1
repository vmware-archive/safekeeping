[CmdletBinding()]
Param(
    [Parameter(Mandatory = $true)][string] $url,
    [Parameter(Mandatory = $false)][string] $Version
	
)
#Set-StrictMode -Version 3
$ErrorActionPreference = "Stop"


function download([String] $Version) {

    $a = $Version.split(".") 
    $Major = $a[0]
    $Minor = $a[1]
    $Patch = $a[2]
    $Build = $a[3]

    $Version = "$Major.$Minor"
    $BaseDir = $(Get-ChildItem -Path $MyInvocation.MyCommand.Path).DirectoryName 
    if ($BaseDir -is [system.array]) { $BaseDir = $BaseDir[0] }
    if ($isWindows) {
        $Platform = "windows"
        $DownloadFile = "VMware-vix-disklib-$Version.$Patch-$Build.x86_64.zip"
    }
    else {
        $Platform = "linux"
        $DownloadFile = "VMware-vix-disklib-$Version.$Patch-$Build.x86_64.tar.gz" 
    } 
    $VddkDirectory = Join-Path -Path $BaseDir -ChildPath $Platform -AdditionalChildPath "vddk" 
    if ( -not (Test-Path $VddkDirectory)) {
        New-Item -ItemType directory -Path $VddkDirectory
    } 
    $OutFile = $(Join-Path -Path "$VddkDirectory" -ChildPath "$DownloadFile") 
    Write-host "Downloading: $Platform Version: $Version.$Patch-$Build    " -NoNewline
    Invoke-WebRequest -Uri "$url/build/mts/release/bora-$build/publish/$DownloadFile" -OutFile $OutFile 
    Write-Host "Done."
}
if ($Version ) {
    downloads $Version
}
else {
    $Versions = @("7.0.3.20134304","7.0.3.19513565"   , "7.0.3.18705163", "7.0.2.17696664", "7.0.1.16860560", "7.0.0.15832853", "6.7.3.14389676", "6.7.2.13015725", "6.7.1.10362358", "6.7.0.8535999", "6.7.0.8173251", "6.5.4.13861102", "6.5.3.8315684", "6.5.2.6195444")
    foreach ($v in $Versions) {
        download $v
    } 
}
#Remove-Item -path vddk -Recurse
