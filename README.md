

# safekeeping

## Overview
Safekeeping is an online backup tool for VMware vSphere.
Features:
1. Generation base repository
2. Command-line Interface, Interactive mode, and batch mode.
3. Support for Virtual Machine, Improved Virtual Disk, vApp and K8s (experimental).
4. vCenter Tag support and management.
5. Archive Compression.
6. Different repository target support (AWS S3, File System, NFS  and more to come).
7. Restore to a New Virtual Machine new vApp or a new Improved Virtual Disk.
9. Recovery of a Virtual Machine.
10. Support VM vApp property.
11. Support for encrypted VM and IVD.
12. Support for Storage Profile.
13. Support for VM implementing Microsoft VBS.
14. VMware Cloud on AWS and FedRamp support.
15. Kubernetes backup support.

## Try it out

### Prerequisites
 
* Prereq CentOS 7.x with the following packages: open-vm-tools wget java-1.8.0-openjdk ant gcc zip gcc-c++ 

### Build & Run

1. Extract the tar file  
2. copy the VDDK tar.gz inside the folder packages
3. run ant configure
4. 

## Documentation

## Contributing

The safekeeping project team welcomes contributions from the community. Before you start working with safekeeping, please
read our [Developer Certificate of Origin](https://cla.vmware.com/dco). All contributions to this repository must be
signed as described on that page. Your signature certifies that you wrote the patch or have the right to pass it on
as an open-source patch. For more detailed information, refer to [CONTRIBUTING.md](CONTRIBUTING.md).

## License
