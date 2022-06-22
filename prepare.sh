#!/bin/sh
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
 
cleanUp() {
    echo Starting CleanUp
    if [ -d "$jdkDir" ] 
    then
       rm -rf  "$jdkDir"
    fi
    if [ -d "$BaseDirectory/gradle.properties" ] 
    then
       rm -rf  "$BaseDirectory/gradle.properties"
    fi
    if [ -d "$BaseDirectory/build.gradle" ] 
    then
       rm -rf  "$BaseDirectory/build.gradle"
    fi

    if [ -d "$cxfJre" ] 
    then
       rm -rf  "$cxfJre"
    fi

    if [ -d "$cmdJre" ] 
    then
       rm -rf  "$cmdJre"
    fi
}

defineVariable() {
    # Absolute path to this script, e.g. /home/user/bin/foo.sh
    SCRIPT=$(readlink -f "$0")
    # Absolute path this script is in, thus /home/user/bin
    BaseDirectory=$(dirname "$SCRIPT") 
    jdkDir="$BaseDirectory/jdk" 
    jdkDistribDir="$BaseDirectory/jdk/dist"  
    JavaHome="$BaseDirectory/jdk"
    javaDistribution="$JavaHome/dist"
     
    cxfDist="$BaseDirectory/safekeeping-cxf/src/dist"
    cmdDist="$BaseDirectory/safekeeping-cmd/src/dist"

    cxfJre="$cxfDist/jre"
    cmdJre="$cmdDist/jre"

    JreWindows="$jdkDistribDir/jre/win"
    jreLinux="$jdkDistribDir/jre/linux"
}
  
case "$1" in
    -jdk8)
        JdkVersion="VERSION_1_8"
        ver="8u282"
        rev="08"
        VR=$ver"b"$rev
        jdkFolder="jdk$ver-b$rev"
        jreFolder="$jdkFolder-jre"
        openJdkUrlPath="https://github.com/adoptium/temurin8-binaries/releases/download/jdk$ver-b$rev"
        jreWinFileName="OpenJDK8U-jre_x64_windows_hotspot_$VR.zip"
        jreLinuxFileName="OpenJDK8U-jre_x64_linux_hotspot_$VR.tar.gz"
        jdkWinFileName="OpenJDK8U-jdk_x64_windows_hotspot_$VR.zip"
        jdkLinuxFileName="OpenJDK8U-jdk_x64_linux_hotspot_$VR.tar.gz"
    ;;
    -jdk11)
        JdkVersion="VERSION_11" 
        ver="11.0.10"
        rev="9"
        VR=$ver"_"$rev
        jdkFolder="jdk$ver-b$rev"
        jreFolder="$jdkFolder-jre"
        openJdkUrlPath="https://github.com/adoptium/temurin11-binaries/releases/download/jdk-$ver%2B$rev
        jreWinFileName="OpenJDK11U-jre_x64_windows_hotspot_$VR.zip"
        jreLinuxFileName="OpenJDK11U-jre_x64_linux_hotspot_$VR.tar.gz"
        jdkWinFileName="OpenJDK11U-jdk_x64_windows_hotspot_$VR.zip"
        jdkLinuxFileName="OpenJDK11U-jdk_x64_linux_hotspot_$VR.tar.gz" 
   ;;
    -clean)
        defineVariable
        cleanUp 
        echo Done
        exit
    ;;
    *) 
        echo  Configure Safekeeping to use a specific OpenJava version:
        echo  -jdk8        Use Java 1.8 
        echo  -jdk11       Use Java 11 
        echo  -clean       Reverse any change made by the script
       exit
    ;;
esac


 
echo "Configure Safekeeping to use Java version :$JdkVersion"
 

 

 
defineVariable
cleanUp

#download JDK
mkdir $jdkDir 
echo  wget  --directory-prefix="$jdkDir"  "$openJdkUrlPath/$jdkLinuxFileName"
wget  --directory-prefix="$jdkDir"  "$openJdkUrlPath/$jdkLinuxFileName"
echo tar    -C "$jdkDir" -zxf "$jdkDir/$jdkLinuxFileName"
tar    -C "$jdkDir" -zxf "$jdkDir/$jdkLinuxFileName"
cd "$jdkDir" 
for c in  jdk*; do   
    mv  "$c"/*  . 
done
cd "$BaseDirectory"
rm "$jdkDir/$jdkLinuxFileName"
rmdir "$jdkDir/$c"


mkdir -p "$jdkDistribDir"
mkdir -p "$JreWindows"
mkdir -p "$jreLinux"


#Windows distribution 
wget  --directory-prefix="$jdkDistribDir"  "$openJdkUrlPath/$jreWinFileName"  
unzip -q "$jdkDistribDir/$jreWinFileName" -d "$JreWindows"
cd "$JreWindows" 
for c in  jdk*; do   
    mv  "$c"/*  . 
done
cd "$BaseDirectory"
rmdir "$JreWindows/$c"
rm "$jdkDistribDir/$jreWinFileName"


#Linux distribution  
wget  --directory-prefix="$jdkDistribDir"  "$openJdkUrlPath/$jreLinuxFileName"  
tar    -C "$jreLinux" -zxf "$jdkDistribDir/$jreLinuxFileName"
cd "$jreLinux" 
for c in  jdk*; do   
    mv  "$c"/*  . 
done
cd "$BaseDirectory"
rm "$jdkDistribDir/$jreLinuxFileName"
rmdir "$jreLinux/$c" 


mkdir -p "$cxfDist"
mkdir -p "$cmdDist"

cp -r "$jdkDistribDir/jre"  "$cxfDist" 
cp -r "$jdkDistribDir/jre"  "$cmdDist" 
 
cat > "$BaseDirectory/gradle.properties" <<- EOM
org.gradle.java.home=$JavaHome
org.gradle.caching=true
org.gradle.vfs.watch=true
openJdkUrlPath=$openJdkUrlPath
jreWinFileName=$jreWinFileName
jreLinuxFileName=$jreLinuxFileName
jdkWinFileName=$jdkWinFileName
jdkLinuxFileName=$jdkLinuxFileName
jreFolder=$jreFolder
javaDistribution=$javaDistribution
EOM


 
cat > $BaseDirectory/build.gradle  << BUILD
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
             sourceCompatibility = JavaVersion.$JdkVersion
             targetCompatibility = JavaVersion.$JdkVersion
    }
}

task info {
    doLast {
       println  "Root project:   \${project.rootProject}";
       println  "  rootDir:      \${project.rootDir}"
       println  "  projectDir:   \${project.projectDir}";
       println  "  project dir:  \${System.getProperty("user.dir")}";
    }
}
BUILD


echo  "Done - Use ./gradlew installDist to build Safekeeping"

 