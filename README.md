

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
 
Prereq CentOS 7.x with the following packages: 
- [ ] open-vm-tools 
- [ ] wget 
- [ ] java-1.8.0-openjdk 
- [ ] ant 
- [ ] gcc 
- [ ] zip 
- [ ] gcc-c++ 

### Build & Run

- Extract the Safekeeping source tar file  
- Download the [Virtual Disk Development Kit](https://code.vmware.com/web/sdk/6.7/vddk) 
- copy or link the _VMware-vix-disklib-6.x.y-zzzzz.x86_64.tar.gz_ to the folder **_safekeeping-1.x.y/packages_**
- The following steps will work on most recent Linux distributions:
```
ant configure
ant install
```

## Documentation

## Contributing

The safekeeping project team welcomes contributions from the community. Before you start working with safekeeping, please
read our [Developer Certificate of Origin](https://cla.vmware.com/dco). All contributions to this repository must be
signed as described on that page. Your signature certifies that you wrote the patch or have the right to pass it on
as an open-source patch. For more detailed information, refer to [CONTRIBUTING.md](CONTRIBUTING.md).

## License
