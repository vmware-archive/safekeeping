#!/bin/bash
#
# Copyright 2008 VMware, Inc.  All rights reserved. 
#
# convert a directory to an rpm or debian package containing that layout
#

BUILD_DIR=.build
BUILD_PATH=$PWD/$BUILD_DIR
DEBIAN_DIR=$BUILD_DIR/DEBIAN
POSTINST=postinst
PREINST=preinst
POSTRM=postrm
PRERM=prerm
SOURCE_DIR=
HSPEC=.config.spec
DEB_CONTROL=control
RPM_CONTROL=control.spec
CONTROL=
TYPE=
template=0

trap "rm -rf $BUILD_DIR; exit 1" 1 2 3 14 15

                                
stderr()
{
    builtin echo -e "$*" 1>&2
}

usage()
{
    stderr
    stderr Usage:
    stderr "\t$pgm -t {rpm|deb} [-T] [-c control] [-P preinst] [-p postinst]"
    stderr "\t\t [-R prerem] [-r postrem] dir"
    stderr
    stderr '\t\t -t \tType of package to create (rpm or deb).'
    stderr
    stderr '\t\t -T \tA sample control file to be used as a template will be'
    stderr '\t\t    \tcreated for the package type.'
    stderr
    stderr '\t\t -c \tPackage control file. A sample control file to be'
    stderr '\t\t    \tused as a template can be created by using the -T'
    stderr '\t\t    \toption.'
    stderr
    stderr '\t\t -P \tPre-installation script to be run before the package'
    stderr '\t\t    \tis installed.'
    stderr
    stderr '\t\t -p \tPost-installation script to be run after the package'
    stderr '\t\t    \tis installed.'
    stderr
    stderr '\t\t -R \tPre-removal script to be run before the package'
    stderr '\t\t    \tis removed.'
    stderr
    stderr '\t\t -r \tPost-removal script to be run after the package is'
    stderr '\t\t    \tremoved.'
    stderr
    stderr '\t\t dir\tDirectory to be packaged. Programs and files in this'
    stderr '\t\t    \tdirectory should be in their proper place when'
    stderr '\t\t    \tinstalled, relative to this directory. For example,'
    stderr '\t\t    \ta file that should be installed into the /etc directory'
    stderr '\t\t    \tshould be put into a subdirectory called etc in the'
    stderr '\t\t    \tsupplied directory.'
    
}

#
# Make a template packaging control file
#
# $1 == package type
# $2 == filename to create
#
make_template()
{    
     if [ -f $2 ]
     then
        stderr $pgm: will not overwrite existing file \"$2\"
        return 1
     fi

    case "$1" in
        rpm)
            (    
            echo Summary: This is the short description of the package.
            echo Name: change-this-package-name
            echo Version: 99.99
            echo Release: 99.99
            echo License: Commercial
            echo Vendor: Your Company Name
            echo Group: System Environment/Daemons
            echo URL: http://YourCompanyURL
            echo BuildArchitectures: noarch
            echo \#Requires:
            echo
            echo %description
            echo This is the longer description of the package, and
            echo should contain more detailed informaton about what the
            echo package provides. 
            echo
            echo Dependencies are specified in this file by using the
            echo Requires tag shown above. The Requires tag should be
            echo uncommented \(remove the \# character in front of Requires\),
            echo and packages upon which this package depends should be
            echo listed after the Requires keyword, separated by commas \(,\).
            echo For more information about specifying package dependencies,
            echo please see
            echo http://www.rpm.org/max-rpm/s1-rpm-depend-manual-dependencies.html
            echo
            if [ -f $POSTINST ]
            then
                echo %post
                cat $POSTINST
                echo
            fi

            if [ -f $PREINST ]
            then
                echo %pre
                cat $PREINST
                echo
            fi

            if [ -f $PRERM ]
            then
                echo %preun
                cat $PRERM
                echo
            fi

            if [ -f $POSTRM ]
            then
                echo %postun
                cat $POSTRM
                echo
            fi
            echo 
            echo \#
            echo \# Do not put anything below the %defattr line\; the list of files
            echo \# in this package are automatically written there.
            echo \#
            echo %files
            echo '%defattr(-,root,root)'
            ) > $2
        ;;

        deb)
            (
            echo Package: change-this-package-name
            echo Version: 99.99
            echo Essential: no
            echo Priority: extra
            echo Section: utils
            echo Maintainer: yourname@yourcompany.com
            echo Architecture: i386
            echo Depends:
            echo Description: This is the short description of the package.
            echo "  This is the longer description of the package, and"
            echo "  should contain more detailed informaton about what the"
            echo "  package provides. Dependencies are specified in this file"
            echo "  by using the Depends tag shown above. Packages upon which"
            echo "  this package depends should be listed after the Depends"
            echo "  keyword, separated by commas (,). For more information"
            echo "  about specifying package dependencies, please see"
            echo "  http://www.debian.org/doc/debian-policy/ch-relationships.html"
            ) > $2
        ;;

        *) stderr $pgm: Unknown type of package: \"$1\"
           ;;
    esac
}

pgm=`basename $0`

while getopts c:p:P:R:r:t:T c
do
    case $c in
        T)  template=1          ;;
        t)  TYPE=$OPTARG        ;;
        c)  CONTROL=$OPTARG     ;;
        d)  SOURCE_DIR=$OPTARG  ;;
        p)  POSTINST=$OPTARG    ;;
        P)  PREINST=$OPTARG     ;;
        r)  POSTRM=$OPTARG      ;;
        R)  PRERM=$OPTARG       ;;
        ?)  usage; exit 1       ;;
    esac
done

shift $(($OPTIND - 1))

SOURCE_DIR=$1

if [ "$TYPE" = "" ]
then
    stderr $pgm: Package type must be specified.
    usage; exit 1
fi

if [ "$TYPE" != "rpm" -a "$TYPE" != "deb" ]
then
    stderr $pgm: Package type must be either rpm or deb.
    usage; exit 1
fi

if [ "$TYPE" = "deb" -a "$CONTROL" = "" ]
then
    CONTROL=$DEB_CONTROL
fi

if [ "$TYPE" = "rpm" -a "$CONTROL" = "" ]
then
    CONTROL=$RPM_CONTROL
fi

if [ $template -eq 1 ]
then
    make_template $TYPE $CONTROL
    ret=$?
    if [ $ret -eq 0 ]
    then
        echo $TYPE control file template has been created as \"$CONTROL\"
    fi
    exit $ret
fi


if [ "$SOURCE_DIR" = "" ]
then
    stderr $pgm: Directory to convert must be specified.
    usage; exit 1
fi

if [ ! -d "$SOURCE_DIR" ]
then
    stderr $pgm: Directory \"$SOURCE_DIR\" does not exist.
    usage; exit 1
fi

        
rm -rf $BUILD_DIR

if [ ! -f $CONTROL ]
then
    stderr A packaging control file named \"$CONTROL\" was not found. A template
    stderr control file named \"$CONTROL\" will be created in the current directory.
    stderr This control file should be modified appropriately for your package,
    stderr after which this command should be run again to generate the package.

    make_template $TYPE $CONTROL
    exit $?
fi

case "$TYPE" in
    rpm)
        #
        # The preinstall, postinstall, prerm and postrm sections of
        # the rpm spec file have already been constructed above from
        # the pre and post files found
        #
        ;;

    deb)
        mkdir -p $DEBIAN_DIR
        cp $CONTROL $DEBIAN_DIR/control
        #
        # Debian insists that install and remove scripts are exec'able,
        # so help folks out and add the #! to the front of it
        #
        if [ -f $POSTINST ]
        then
            (
                echo '#!/bin/bash'
                cat $POSTINST
            ) > $DEBIAN_DIR/postinst

            chmod +x $DEBIAN_DIR/postinst
        fi


        if [ -f $PREINST ]
        then
            (
                echo '#!/bin/bash'
                cat $PREINST
            ) > $DEBIAN_DIR/preinst

            chmod +x $DEBIAN_DIR/preinst
        fi

        if [ -f $PRERM ]
        then
            (
                echo '#!/bin/bash'
                cat $PRERM
            ) > $DEBIAN_DIR/prerm

            chmod +x $DEBIAN_DIR/prerm
        fi

        if [ -f $POSTRM ]
        then
            (
                echo '#!/bin/bash'
                cat $POSTRM
            ) > $DEBIAN_DIR/postrm

            chmod +x $DEBIAN_DIR/postrm
        fi
        ;;
esac

( cd $SOURCE_DIR; find . -print | cpio -pdum $BUILD_PATH )

case "$TYPE" in
    deb)
        fakeroot dpkg-deb -b $BUILD_DIR $PWD
        ;;

    rpm)
        PNAME=`sed -n 's/Name: \(.*\)/\1/p' $CONTROL`
        VERSION=`sed -n 's/Version: \(.*\)/\1/p' $CONTROL`
        RELEASE=`sed -n 's/Release: \(.*\)/\1/p' $CONTROL`
        ARCH=`sed -n 's/BuildArchitectures: \(.*\)/\1/p' $CONTROL`
        (
          cat $CONTROL
          ls -1d $BUILD_DIR/* | sed 's,^'$BUILD_DIR,,
        ) > $HSPEC
        rpmbuild -bb $HSPEC \
                --buildroot $BUILD_PATH \
                --define "_use_internal_dependency_generator 0" \
                --define "__find_requires %{nil}" \
                --define "_topdir $BUILD_PATH" \
                --define "_rpmdir $PWD" \
                --define '_builddir %{_topdir}' \
                --define '_sourcedir %{_topdir}' \
                --define "_rpmfilename ${PNAME}_${VERSION}-${RELEASE}_${ARCH}.rpm"; \
        rm $HSPEC
        ;;
esac        

ret=$?

rm -rf $BUILD_DIR

exit $ret

