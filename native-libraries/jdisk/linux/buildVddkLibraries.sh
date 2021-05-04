#!/bin/bash
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

build(){
    VERSION=$MAJOR.$MINOR 
    VDDK_DIRECTORY="./vddk"
    TAR_FILE=VMware-vix-disklib-$VERSION.$PATCH-$BUILD.x86_64.tar.gz
    JVIX_RESOURCES_DIRECTORY="../../../../jvix/src/main/resources" 
    echo "Checking $VDDK_DIRECTORY/$TAR_FILE file"
    if [ -f "$VDDK_DIRECTORY/$TAR_FILE" ]; then
        tar -zxvf  "$VDDK_DIRECTORY/$TAR_FILE" vmware-vix-disklib-distrib/lib64   
        if [ -d  vmware-vix-disklib-distrib/lib64 ]; then
            if [ -d "$VERSION/lib" ]; then
                rm -rf "$VERSION/lib"
            fi 
            mkdir -v -p $VERSION/lib 
            mv vmware-vix-disklib-distrib/lib64 $VERSION/lib/
            rmdir vmware-vix-disklib-distrib
            pushd $VERSION
            make -f makefile
            cd lib/lib64
            tar -cvf $BUILD.tar -C $PWD .
             
            mkdir -p $JVIX_RESOURCES_DIRECTORY/x64/$MAJOR/$MINOR/linux/$PATCH
            cp -v $BUILD.tar  $JVIX_RESOURCES_DIRECTORY/x64/$MAJOR/$MINOR/linux/$PATCH 
            cd ../..
            make clean
            popd
            echo "Clean Lib Directory $VERSION/lib/"
            rm -rvf $VERSION/lib/
             
        else
            echo " vmware-vix-disklib-distrib/lib64 doesn't exist"
        fi
    else
            echo "$VDDK_DIRECTORY/$TAR_FILE  doesn't exist"
    fi

}
if [ "$#" -eq 1 ]; then
    IFS='.' # dot (.) is set as delimiter
    read -ra ADDR <<< "$1" # str is read into an array as tokens separated by IFS
    MAJOR="${ADDR[0]}"
    MINOR="${ADDR[1]}"
    PATCH="${ADDR[2]}"
    BUILD="${ADDR[3]}" 
    IFS=
    
    build
else
FILES=./vddk/*.tar.gz
for f in $FILES
do
  echo "Processing $f ..."
  VERSION=`grep -oP '(?<=VMware-vix-disklib-).*?(?=.x86_64.tar.gz)' <<< $f`
  echo Building Version:$VERSION 
  IFS='.' # dot (.) is set as delimiter
    read -ra ADDR <<< "${VERSION/-/.}" # str is read into an array as tokens separated by IFS
    MAJOR="${ADDR[0]}"
    MINOR="${ADDR[1]}"
    PATCH="${ADDR[2]}"
    BUILD="${ADDR[3]}" 
    IFS=
    
    build 
done

fi
 
 

