![logo](https://github.com/vmware/safekeeping/raw/assets/228456-vmw-os-lgo-safekeeper-final-sml.png)
# safekeeping  

## Overview
Safekeeping is an online backup tool for VMware vSphere.
There are two way to use Safekeeping 
1. by command line using safekeeping-cmd
2. by Soap WebService using safekeeping-cxf daemon

## Common features between cmd and cxf:
1. Generation base repository.
2. Support for Virtual Machine, Improved Virtual Disk, vApp and K8s (experimental).
3. vCenter Tag support and management.
4. Archive Compression.
5. Archive Encryption
6. Multi repository target support (AWS S3,Azure, File System, and more to come).
7. Restore to a New Virtual Machine new vApp or a new Improved Virtual Disk.
9. Full Recovery of a Virtual Machine and VAPP with identity (UUID and other metadata).
10. Support VM and VAPP vApp property(OVF).
11. Support for encrypted VM and IVD.
12. Support for Storage Profile.
13. Support for VM implementing Microsoft VBS.
14. VMware Cloud on AWS and FedRamp support.
15. Kubernetes backup support (work in progress).
16. Full and incremental backup
17. Data dedup on the fly
18. Virtual full backup - forever incremental (create a full backup generation directly on the repository) 

## safekeeping-cmd Features
1. Command-line Interface, Interactive mode, and batch mode.
2. Single user 
3. single thread
4. Support scripts 
5. Extended command support for any entity on vCenter (more or less like powercli) 
6. Works on Windows and Linux 

## safekeeping-cxf Features
1. Soap Web Service
2. Multi concurrent users with different logins 
3. authentication based on vSphere PSC or IVDM
4. multiThreads operations
5. multi targets supports on the same sessions (ex backup to 2 different S3 buckets)
6. Rich SOAP Api   
7. Works on Windows and Linux 

## Try it out

### Prerequisites
CentOS 7.x with the following packages: 
- [ ] open-vm-tools 
- [ ] wget 
- [ ] java-1.8.0-openjdk 
- [ ] gradle 
- [ ] gcc 
- [ ] zip 
- [ ] gcc-c++ 

Note: Other distributions should work fine 

Windows (any) with the following packages: 
- [ ] powershell   
- [ ] java-1.8.0-openjdk 
- [ ] gradle 
- [ ] Visual Studio 2017 or 2019  



# Build 

- Extract the Safekeeping source tar file  or sincronize from this GIT deposit
- Download the [Virtual Disk Development Kit](https://code.vmware.com/web/sdk/7.0/vddk) and copy on the files on safekeeping/jdisklib/"OS-type"/vddk  
- 
## Build the VDDK Wrapper

#### on Windows 
```
cd safekeeping\jdisklib\windows 
```
Copy any VDDK library you want to use inside the **safekeeping\jdisklib\windows\vddk** directory or download any compatible version with ```_VMwareInternalDownload.ps1```

To build the VDDK wrapper use
- with VisualStudio 2017    ```build-2017.cmd``` 
- with VisualStudio 2019    ```build-2019.cmd``` 

#### on Linux 
```
cd ./safekeeping/jdisklib/linux
```

Copy any VDDK library you want to use inside the **./safekeeping/jdisklib/linux/vddk** directory 

To build the VDDK wrapper use:	```./buildVddkLibraries.sh```    


## Build Java code
***Important JAVA supported versions are jre1.8.0_251, jre1.8.0_261, or greater anything over Java 11 doesn't work.***

#### on Windows 
```
cd safekeeping
configure.cmd <java_version>  
gradle build 
``` 
#### on Linux 
```
cd safekeeping
configure.sh <java_version>  
gradle build 
``` 
<java_version> 
8 jdk1.8
9 jdk1.9
10 jdk10
11 jdk11



# Gradle main tasks 
**Application tasks** 
- **run** - Runs this project as a JVM application

**Build tasks** 
- **build** - Assembles and tests this project.
- **clean** - Deletes the build directory.

**safekeeping-cmd and safekeeping-cxf only tasks**
- **deb** - Safekeeping Installation. - Create a deb file (requires Debian/Ubuntu)
- **dmg** - Safekeeping Installation. - CmdLine Version (requires MacOS )
- **msi** - Safekeeping Installation. - CmdLine Version (requires Windows and https://wixtoolset.org/ )
- **rpm** - Safekeeping Installation. - CmdLine Version (requires ReadHat/Centos )


## Run
### safekeeping-cxf
Run the webservice in interactive mode using port 8080 for HTTP and 8043 for HTTPS
```
safekeeping-cxf -port 8080 -secure 8043 -interactive
```

### safekeeping-cmd
#### To run in interactive mode 
```
safekeeping
or 
/opt/vmware/safekeeping/safekeeping
```
#### To execute a command  
```
safekeeping <command> <options>
```
#### For example to run a backup  
```
safekeeping backup vm:myTestVM
```
### Help
If you are looking for help, the following configure command will display a list of help options:
```
safekeeping --help
```

## Documentation
Check the [wiki](https://github.com/vmware/safekeeping/wiki) for safekeeping-cmd 

## Blogs Articles
[Cormac Hogan Blog](https://cormachogan.com/2019/11/13/safekeeping-a-useful-tool-for-managing-first-class-disks-improved-virtual-disks)

## Directory Contents
### Directories
 
- **safekeeping-cmd**
  - Safekeeping Command Line version 
- **safekeeping-cxf**
  - Safekeeping SOAP Web Service daemon version
- **safekeeping-core**
  - Core backup tool library written with Java
- **jdisklib**
  - Native interface for VDDK written with C++ using VDDK library.
- **jvix**
  - Java wrapper for *jdisklib* native library
- **safekeeping-common**
  - Java common library used by the project
- **safekeeping-core-ext**
  - Library used by the safekeeping-cmd to deal with: IVD, snapshots, etc. 
- **safekeeping-external**
  - Wrapper of the internal class. Used by safekeeping-cfx for reflection
- **jopt-simple**
  - Modified [jopt-simple parsing command line](http://jopt-simple.github.io/jopt-simple/) library
  **nssm**
  - Updated NSSM - the Non-Sucking Service Manager (support Visual Studio 2017 and 2019)
  **PowerShell**
  - Safekeeping SAPI cmd-let 
- **lib**
  - Support files 
- **jar**
  - Java jar files required by Safekeeping  
- **doc**
  - Documentation.
- **sample_scripts**
  - Sample scripts to be used with Safekeeping
- **nfs-client**
  - NFS library ***DEPRECATED***
### Files
- **LICENSE.txt**
  - BSD-2 License file.
- **open_source_licenses.txt**
  - Open source License file.
- **README.md**
  - This file.
- **VERSION**
  - Safekeeping version number
- **build.gradle**
  - Gradle master build script
- **settings.gradle**
  - Gradle settings file


## Contributing

The safekeeping project team welcomes contributions from the community. Before you start working with safekeeping, please
read our [Developer Certificate of Origin](https://cla.vmware.com/dco). All contributions to this repository must be
signed as described on that page. Your signature certifies that you wrote the patch or have the right to pass it on
as an open-source patch. For more detailed information, refer to [CONTRIBUTING.md](CONTRIBUTING.md).

## License
[BSD-2 License](https://github.com/vmware/safekeeping/blob/master/LICENSE.txt)

