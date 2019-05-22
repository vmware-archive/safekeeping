#!/bin/sh
 

#VDDK_LIBRARY=${1-"VDDK_LIBRARY"}
#JAVA_LIBRARY=${2-"JAVA_LIBRARY"}
JAVA_LIBRARY=`ls ./packages/jdk-8u* 2>/dev/null`
echo JAVA_LIBRARY= $JAVA_LIBRARY
VDDK_LIBRARY=`ls ./packages/VMware-vix-disklib-*`
echo JAVA_LIBRARY
 
#REPOSITORY=$(cd $(dirname $0);pwd)
REPOSITORY=$(pwd)
echo $REPOSITORY

JDKDIR=$REPOSITORY/jdk
LIB_BASEDIR=$REPOSITORY/jdisklib/lib 
CCODE_BASEDIR=$REPOSITORY/jdisklib/src/
INCLUDE_BASEDIR=$CCODE_BASEDIR/include 
VMBKVERSION_JAVA=$REPOSITORY/vmbk/src/com/vmware/vmbk/control/VmbkVersion.java
VDDK_CONF=$REPOSITORY/vmbk/conf/vddk.conf  
mkdir $LIB_BASEDIR 

echo Preparing Java Runtime 
if [ ${#JAVA_LIBRARY} -gt 0 ];then
	read   ss
	echo tar -zxvf  $JAVA_LIBRARY -C $REPOSITORY
	tar -zxvf  $JAVA_LIBRARY -C $REPOSITORY
	mv $REPOSITORY/jdk* $JDKDIR
else 
	echo use OpenJDK
	mkdir -p $JDKDIR/jre
	cp -r -l /usr/lib/jvm/jre-1.8.0/* $JDKDIR/jre
fi 


echo Preparing VDDK Library
echo tar -zxvf $VDDK_LIBRARY
tar -zxvf $VDDK_LIBRARY
cp -av vmware-vix-disklib-distrib/lib64/ $LIB_BASEDIR
cp -av vmware-vix-disklib-distrib/lib32/ $LIB_BASEDIR
cp -av vmware-vix-disklib-distrib/include/* $INCLUDE_BASEDIR

if [ ! -f $LIB_BASEDIR/lib64/libcrypto.so ];then
 cd $LIB_BASEDIR/lib64/
 ln -s libcrypto.so.1.0.2 libcrypto.so
 cd $REPOSITORY
fi

MAJOR=`basename $VDDK_LIBRARY | sed 's/VMware-vix-disklib-\([0-9]\)\.\([0-9]\)\.\([0-9][0-9]*\).*/\1/'`
MINOR=`basename $VDDK_LIBRARY | sed 's/VMware-vix-disklib-\([0-9]\)\.\([0-9]\)\.\([0-9][0-9]*\).*/\2/'`
PATCH=`basename $VDDK_LIBRARY | sed 's/VMware-vix-disklib-\([0-9]\)\.\([0-9]\)\.\([0-9][0-9]*\).*/\3/'`
BUILD=`basename $VDDK_LIBRARY | sed 's/VMware-vix-disklib-\([0-9]\)\.\([0-9]\)\.\([0-9][0-9]*\)\-\([0-9][0-9]*\).*/\4/'`

echo "VDDK File $1"
echo "VDDK VERSION: $MAJOR . $MINOR . $PATCH    BUILD $BUILD"  

EXTVERSION=$MAJOR.$MINOR.$PATCH.$BUILD
echo $EXTVERSION>./VDDK_VERSION
VERSION=$(($MAJOR*10000+$MINOR*1000+10*$PATCH))


cp $CCODE_BASEDIR/JDISKLIB_$MAJOR.$MINOR.version  $CCODE_BASEDIR/jDiskLib.c
cp $INCLUDE_BASEDIR/JDISKLIBIMPL_$MAJOR.$MINOR.version  $INCLUDE_BASEDIR/jDiskLibImpl.h
ln -s $REPOSITORY/jdisklib/Makefile-unix $REPOSITORY/jdisklib/Makefile

ln -s $CCODE_BASEDIR/Makefile-unix $CCODE_BASEDIR/Makefile 

sed -e 's/XXXXXXXX/'"$VERSION"'/g' -e 's/YYYYYYYY/'"$EXTVERSION"'/g' $INCLUDE_BASEDIR/VIX_DISK_LIB_VERSION.TEMPLATE> $INCLUDE_BASEDIR/vixDiskLibVersion.h

sed -e "s|XXXXXXXXXX|`cat ./VERSION`|"  -e "s|VVVVVVVVVV|`cat ./VDDK_VERSION`|" $VMBKVERSION_JAVA.template >$VMBKVERSION_JAVA


