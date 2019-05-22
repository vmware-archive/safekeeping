# Comments start with hash sign. Caution: macros in comments are still
# expanded, which can lead to unexpected results. To stop macro from expanding,
# double its percent sign (e.g. %%mvn_install)

# First part usually consists of metadata such as package name, version, and
# much more
Name:		vmbk
Version:	1.0
Release:	4%{?dist}
Summary:	Vvmb Backup simple solution

License:	GPLv2+
# Homepage URL of the project
URL:		http://www.vmware.com

# Our source archive. %{name} expands to 'helloworld' so the resulting
# tarball name would be 'helloworld.tar.gz'.
Source0:    %{name}.tar.gz

# Packages that contain only architecture independent files, such as shell
# scripts or regular Java programs (not JNI libraries), should be marked as 'noarch'
BuildArch:  x86_64

# Project's build time dependency. We don't really need JUnit, it just
# serves as and example
BuildRequires: ant

%description
VMBK Backup Solution

%prep
# section for preparation of sources, applying patches
# or other things which can be done before running the build
# The macro setup is used to unpack sources
%setup -q 

%build
# Section for compiling and generally assembling the final pieces.
# Our Makefile builds the project JAR file
#ant build  

%install
# Installation into directory prepared by RPM expressed as %{buildroot}
#ant install 

# We use macro %jpackage_script to generate wrapper script for our JAR
# Will be explained in later sections
%jpackage_script org.fedoraproject.helloworld.HelloWorld "" "" %{name} helloworld true

# List of files that this package installs on the system
%files
%/opt/vmbk

%changelog
* Tue Mar 19 2013 Stanislav Ochotnicky <sochotnicky@redhat.com> - 1.0-1
- This is first version
