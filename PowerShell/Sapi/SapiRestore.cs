using System;
using System.Collections; 
using System.Management.Automation; 
using System.Text.RegularExpressions; 
using VMware.VimAutomation.ViCore.Impl.V1.DatastoreManagement;
using VMware.VimAutomation.ViCore.Impl.V1.Inventory;
using VMware.VimAutomation.ViCore.Types.V1.Inventory;

namespace SapiCli
{

    static public class RegexPattern
    {
        public readonly static Regex IP4PATTERN = new Regex("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
        public readonly static Regex UUIDPATTERN = new Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    }
    [Cmdlet(VerbsData.Restore, "VApp")]
    [OutputType(typeof(SapiRestoreResult[]))]
    public class SapiRestoreVapp : SapiRestore
    {

        #region Properties


        [Parameter(Mandatory = false, HelpMessage = "Allow to create duplicate VM name inside a VApp.")]
        [ValidateNotNullOrEmpty()]
        public Boolean AllowDuplicates
        {
            get
            {
                return AllowDuplicatesValue;
            }
            set
            {
                AllowDuplicatesSet = true;
                AllowDuplicatesValue = value;
            }
        }
        [Parameter(Position = 0, Mandatory = true, ValueFromPipeline = true, HelpMessage = "Specifies the vApp you want to Restore.")]

        [ValidateNotNullOrEmpty]
        public String[] VApp { get; set; }
        #endregion Properties

        override protected fcoTarget[] ProcessTargetList()
        {
            var targetList = new fcoTarget[VApp.Length];
            for (int i = 0; i < VApp.Length; i++)
            {
                var fco = new fcoTarget();
                if (VApp[i].StartsWith("resgroup-"))
                {
                    fco.keyType = fcoTypeSearch.VAPP_MOREF;
                }
                else if (RegexPattern.UUIDPATTERN.IsMatch(VApp[i]))
                {
                    fco.keyType = fcoTypeSearch.VAPP_UUID;
                }
                else
                {
                    fco.keyType = fcoTypeSearch.VAPP_NAME;
                }
                fco.keyTypeSpecified = true;
                fco.key = VApp[i];
                targetList[i] = fco;
            }
            return targetList;
        }
    }
    [Cmdlet(VerbsData.Restore, "VM")]
    //    [OutputType(typeof(SapiRestoreResult[]))]
    [OutputType(typeof(IEnumerable[]))]
    public class SapiRestoreVm : SapiRestore
    {
        [Parameter(Position = 0, Mandatory = true, ValueFromPipeline = true, HelpMessage = "Specifies the virtual machines you want to Restore.")]
        public String[] VM { get; set; }

        override protected fcoTarget[] ProcessTargetList()
        {
            var targetList = new fcoTarget[VM.Length];
            for (int i = 0; i < VM.Length; i++)
            {
                var fco = new fcoTarget();
                if (VM[i].StartsWith("vm-"))
                {
                    fco.keyType = fcoTypeSearch.VM_MOREF;
                }
                else if (RegexPattern.UUIDPATTERN.IsMatch(VM[i]))
                {
                    fco.keyType = fcoTypeSearch.VM_UUID;
                }
                else
                {
                    fco.keyType = fcoTypeSearch.VM_NAME;
                }

                fco.keyTypeSpecified = true;
                fco.key = VM[i];
                targetList[i] = fco;
            }
            return targetList;
        }

    }

    abstract public class SapiRestore : CmdletTaskProgress
    {


        private int generationIdValue;
        private Boolean generationIdSet;

        //Use by vapp
        protected Boolean AllowDuplicatesValue;
        protected Boolean AllowDuplicatesSet;

        private string resPoolFilter;


        private string vmFolderFilter;


        protected SapiRestore()
        {
            ImportVmxFile = false;
            Recover = false;
            Overwrite = false;
            PowerOn = false;
            Name = null;
            Prefix = null;
            Postfix = null;
            TransportMode = null;

        }

        //Specifies the host on which you want to create the new virtual machine.


        [Parameter(Mandatory = false, HelpMessage = "Specifies the Datacenter on which you want to restore.")]
        [ValidateNotNullOrEmpty()]
        public Datacenter Datacenter { get; set; }
        [Parameter(Mandatory = false, HelpMessage = "Specifies the Datastore on which you want to restore.")]
        [ValidateNotNullOrEmpty()]
        public Datastore Datastore { get; set; }
        [Parameter(Mandatory = false, HelpMessage = "Specifies the Folder on which you want to restore.")]
        [ValidateNotNullOrEmpty()]
        public Folder Folder { get; set; }
        [Parameter(Mandatory = false, HelpMessage = "Specifies the ESX Host on which you want to restore.")]
        [ValidateNotNullOrEmpty()]
        public VMHost VMHost { get; set; }
       
        [Parameter(Mandatory = false, HelpMessage = "Specifies the Resource Pool on which you want to restore.")]
        [ValidateNotNullOrEmpty()]
        public ResourcePool ResourcePool { get; set; } 

        [Parameter(Mandatory = false, HelpMessage = "Specifies the Networks for each vmnic.")]
        public VMware.VimAutomation.Vds.Types.V1.VDPortgroup[] Networks { get; set; }

        [Parameter(Mandatory = false, HelpMessage = "Specifies the VMDK datastore and policy.")]
        public restoreVmdkOption[] Disks { get; set; }


        [Parameter(Mandatory = false, HelpMessage = "Specifies the Name  on which you want to restore.")]
        [ValidateNotNullOrEmpty()]
        public string Name { get; set; }

        [Parameter(Mandatory = false, HelpMessage = "pecifies the prefix to apply to the name.")]
        [ValidateNotNullOrEmpty()]
        public string Prefix { get; set; }
        [Parameter(Mandatory = false, HelpMessage = "Specifies the postfix to apply to the name.")]
        [ValidateNotNullOrEmpty()]
        public string Postfix { get; set; }


        [Parameter(Mandatory = false, HelpMessage = "Specifies the generation you want to restore.")]
        [ValidateNotNullOrEmpty()]
        public int GenerationId
        {
            get
            {
                return generationIdValue;
            }
            set
            {
                generationIdSet = true;
                generationIdValue = value;
            }
        }
        [Parameter(Mandatory = false, HelpMessage = "Specifies the Transport Mode to use during the restore.")]
        [ValidateSetAttribute(
        "hotadd", "hotadd:nbdssl", "hotadd:nbd", "hotadd:nbdssl:nbd", "nbdssl", "nbdssl:hotadd",
                    "nbdssl:nbd", "nbdssl:hotadd:nbd", "nbdssl:nbd:hotadd", "nbd", "nbd:hotadd",
                    "nbd:hotadd:nbdssl", "nbd:nbdsl:hotadd", "nbd:nbdssl")]
        public string TransportMode { get; set; }
        [Parameter(Mandatory = false)]
        public SwitchParameter ImportVmxFile { get; set; }
        [Parameter(Mandatory = false)]
        public SwitchParameter Recover { get; set; }

        [Parameter(Mandatory = false)]
        public SwitchParameter Overwrite { get; set; }

        [Parameter(Mandatory = false)]
        public SwitchParameter PowerOn { get; set; }

        abstract protected fcoTarget[] ProcessTargetList();
        protected override void ProcessRecord()
        {
            base.ProcessRecord();
            SapiClients.CheckConnection();

            var Options = new restoreOptions();
            if (TransportMode != null)
            {
                Options.requestedTransportMode = TransportMode;
            }
            if (generationIdSet)
            {
                Options.generationIdSpecified = true;
                Options.generationId = GenerationId;
            }


            if (Name != null)
            {
                Options.name = Name;
            }
            if (Prefix != null)
            {
                Options.prefix = Prefix;
            }
            if (Postfix != null)
            {
                Options.postfix = Postfix;
            }



            Options.importVmxFile = ImportVmxFile;
            Options.recover = Recover;
            Options.overwrite = Overwrite;
            Options.powerOn = PowerOn;

            if (Datacenter != null)
            {
                Options.datacenter = new searchManagementEntity
                {
                    searchTypeSpecified = true,
                    searchType = searchManagementEntityInfoType.MOREF,
                    searchValue = (Datacenter as DatacenterImpl).Id.Replace("Datacenter-", "")
                };
            }
            if (Datastore != null)
            {
                Options.datastore = new searchManagementEntity
                {
                    searchTypeSpecified = true,
                    searchType = searchManagementEntityInfoType.MOREF,
                    searchValue = (Datastore as DatastoreImpl).Id.Replace("Datastore-", "")
                };
            }
            if (Folder != null)
            {
                Options.folder = new searchManagementEntity
                {
                    searchTypeSpecified = true,
                    searchType = searchManagementEntityInfoType.MOREF,
                    searchValue = (Folder as FolderImpl).Id.Replace("Folder-", "")
                };
            }

            if (VMHost != null)
            {
                Options.host = new searchManagementEntity
                {
                    searchTypeSpecified = true,
                    searchType = searchManagementEntityInfoType.MOREF,
                    searchValue = (VMHost as VMHostImpl).Id.Replace("VMHost-", "")
                };
            }

            if (ResourcePool != null)
            {
                Options.resourcePool = new searchManagementEntity
                {
                    searchTypeSpecified = true,
                    searchType = searchManagementEntityInfoType.MOREF,
                    searchValue = (ResourcePool as ResourcePoolImpl).Id.Replace("ResourcePool-", "")
                };
            }


            if (Networks != null)
            {
                Options.networks = new searchManagementEntity[Networks.Length];
                for (int i = 0; i < Networks.Length; i++)
                {
                    if (Networks[i] != null)
                    {
                        Options.networks[i] = new searchManagementEntity
                        {
                            searchTypeSpecified = true,
                            searchType = searchManagementEntityInfoType.MOREF,
                            searchValue = Networks[i].Id.Replace("DistributedVirtualPortgroup-", "")
                        };
                    }
                }
            }


            if (Disks != null)
            {
                Options.disks = Disks; 
            }
            //Vapp setting
            if (AllowDuplicatesSet)
            {
                Options.allowDuplicatedVmNamesInsideVappSpecified = true;
                Options.allowDuplicatedVmNamesInsideVapp = AllowDuplicatesValue;
            }

            Options.targetList = ProcessTargetList();

            var ActionTask = SapiClients.client.restoreAsync(Options);

            var results = Progress("Restore", ActionTask);


            int index = 0;
            var returnResult = new SapiRestoreResult[results.Length];
            foreach (var result in results)
            {
                returnResult[index] = new SapiRestoreResult(result);
                if (result is resultActionRestore)
                {
                    returnResult[index].GenerationId = (result as resultActionRestore).generationId;
                    returnResult[index].Item = (result as resultActionRestore).fcoEntityInfo.name;

                }

                ++index;

            }

            WriteObject(returnResult, true);

            // This is what actually "returns" output.
        }



    }
}


