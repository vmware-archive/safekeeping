using System;
using System.Management.Automation; 
using VMware.VimAutomation.ViCore.Types.V1.Inventory;

namespace SapiCli
{

    [Cmdlet(VerbsData.Backup, "VApp")]
    [OutputType(typeof(SapiResult[]))]
    public class SapiBackupVapp : SapiBackup
    {
        #region Properties
        [Parameter(Position = 0, Mandatory = true, ValueFromPipeline = true, HelpMessage = "Specifies the vApp you want to backup.", ParameterSetName = "Backup")]
        [Parameter(Position = 0, Mandatory = true, ValueFromPipeline = true, HelpMessage = "Specifies the vApp you want to consolidate.", ParameterSetName = "Consolidate")]
        [ValidateNotNullOrEmpty]
        public VApp[] VApp { get; set; }
        #endregion Properties

        override protected fcoTarget[] ProcessTargetList()
        {
            var targetList = new fcoTarget[VApp.Length];
            for (int i = 0; i < VApp.Length; i++)
            {
                var fco = new fcoTarget
                {
                    keyType = fcoTypeSearch.VAPP_MOREF,
                    keyTypeSpecified = true,
                    key = (VApp[i] as VMware.VimAutomation.ViCore.Impl.V1.Inventory.VAppImpl).Id.Replace("VirtualApp-", "")
                };
                targetList[i] = fco;
            }
            return targetList;
        }


        protected override InventoryItem Item(int index)
        {
            return VApp[index];
        }
    }
    [Cmdlet(VerbsData.Backup, "VM")]
    [OutputType(typeof(SapiResult[]))]
    public class SapiBackupVm : SapiBackup
    {
        [Parameter(Position = 0, Mandatory = true, ValueFromPipeline = true, HelpMessage = "Specifies the virtual machines you want to backup.", ParameterSetName = "Backup")]
        [Parameter(Position = 0, Mandatory = true, ValueFromPipeline = true, HelpMessage = "Specifies the virtual machines you want to virtual full backup.", ParameterSetName = "VirtualBackup")]
        public VMware.VimAutomation.ViCore.Types.V1.Inventory.VirtualMachine[] VM { get; set; }
        override protected fcoTarget[] ProcessTargetList()
        {
            var targetList = new fcoTarget[VM.Length];
            for (int i = 0; i < VM.Length; i++)
            {
                var fco = new fcoTarget
                {
                    keyType = fcoTypeSearch.VM_MOREF,
                    keyTypeSpecified = true,
                    key = (VM[i] as VMware.VimAutomation.ViCore.Impl.V1.VM.UniversalVirtualMachineImpl).Id.Replace("VirtualMachine-", "")
                };
                targetList[i] = fco;
            }
            return targetList;
        }

        protected override InventoryItem Item(int index)
        {
            return VM[index];
        }
    }




    abstract public class SapiBackup : CmdletTaskProgress
    {
        Boolean CompressValue;
        Boolean CompressSet;
        Boolean CipherValue;
        Boolean CipherSet;
        string TransportModeValue;
        Boolean TransportModeSet;
        queryBlocksOption QueryBlocksOptionValue;
        Boolean QueryBlocksOptionSet;



        private int generationIdValue;
        private Boolean generationIdSet;

        #region Properties


        abstract protected InventoryItem Item(int index);


        [Parameter(Mandatory = true, HelpMessage = "Consolidate instead of a real backup", ParameterSetName = "VirtualBackup")]
        public SwitchParameter VirtualFull { get; set; }

        [Parameter(Mandatory = false, HelpMessage = "Specifies the generation you want to consolidate.", ParameterSetName = "VirtualBackup")]
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
        [Parameter(Mandatory = false, ParameterSetName = "Backup")]
        [ValidateNotNullOrEmpty()]
        public string TransportMode
        {
            get
            {
                return TransportModeValue;
            }
            set
            {
                TransportModeSet = true;
                TransportModeValue = value;
            }
        }
        [Parameter(Mandatory = false, ParameterSetName = "Backup")]
        [ValidateNotNullOrEmpty()]
        public queryBlocksOption QueryBlocksOption
        {
            get
            {
                return QueryBlocksOptionValue;
            }
            set
            {
                QueryBlocksOptionSet = true;
                QueryBlocksOptionValue = value;
            }
        }


        [Parameter(Mandatory = false, ParameterSetName = "Backup")]
        [ValidateNotNullOrEmpty()]
        public Boolean Compress
        {
            get
            {
                return CompressValue;
            }
            set
            {
                CompressSet = true;
                CompressValue = value;
            }
        }
        [Parameter(Mandatory = false, ParameterSetName = "Backup")]
        [ValidateNotNullOrEmpty()]
        public Boolean Cipher
        {
            get
            {
                return CipherValue;
            }
            set
            {
                CipherSet = true;
                CipherValue = value;
            }
        }

        [Parameter(Mandatory = false, ParameterSetName = "Backup", HelpMessage = "Suggested method to collect any changed block in a full backup.")]
        [ValidateNotNull()]
        [ValidateSetAttribute("Allocated", "ChangedAreas", "Full")]
        public string BlocksQuery { get; set; }

        [Parameter(Mandatory = false, ParameterSetName = "Backup")]
        [ValidateNotNull()]
        public virtualMachineQuisceSpec QuisceSpecs { get; set; }


        [Parameter(Mandatory = false, ParameterSetName = "Backup")]
        public SwitchParameter Full { get; set; }
        #endregion Properties

        abstract protected fcoTarget[] ProcessTargetList();
        protected override void ProcessRecord()
        {
            base.ProcessRecord();

            SapiClients.CheckConnection();

            if (VirtualFull)
            {
                var Options = new virtualBackupOptions();
                if (generationIdSet)
                {
                    Options.generationIdSpecified = true;
                    Options.generationId = GenerationId;
                }

                Options.targetList = ProcessTargetList();
                var ActionTask = SapiClients.client.virtualBackupAsync(Options);

                var results = Progress("Virtual Full Backup", ActionTask);
                int index = 0;
                var returnResult = new SapiVirtualBackupResult[results.Length];
                foreach (var result in results)
                {
                    returnResult[index] = new SapiVirtualBackupResult(result)
                    {
                        BackupMode = (result as abstractResultActionVirtualBackup).backupMode,
                        GenerationId = (result as abstractResultActionVirtualBackup).generationId,
                        Items = Item(index)
                    };
                    ++index;
                }
                WriteObject(returnResult, true); // This is what actually "returns" output. 
            }
            else
            {
                var Options = new backupOptions();
                if (TransportModeSet)
                {
                    Options.requestedTransportMode = TransportModeValue;
                    Options.requestedBackupModeSpecified = true;
                }
                if (QueryBlocksOptionSet)
                {
                    Options.queryBlocksOption = QueryBlocksOptionValue;
                    Options.queryBlocksOptionSpecified = true;
                }
                if (CompressSet)
                {
                    Options.compressionSpecified = true;
                    Options.compression = CompressValue;
                }
                if (CipherSet)
                {
                    Options.cipherSpecified = true;
                    Options.cipher = CompressValue;
                }
                if (QuisceSpecs != null)
                    Options.quisceSpec = QuisceSpecs;
                if (Full)
                {
                    Options.requestedBackupMode = backupMode.FULL;
                    Options.requestedBackupModeSpecified = true;
                }
                if (BlocksQuery != null)
                {
                    switch (BlocksQuery)
                    {
                        case "Allocated":
                            Options.queryBlocksOption = queryBlocksOption.ALLOCATED;
                            break;
                        case "ChangedAreas":
                            Options.queryBlocksOption = queryBlocksOption.CHANGED_AREAS;
                            break;
                        case "Full":
                            Options.queryBlocksOption = queryBlocksOption.FULL;
                            break;
                        default:
                            Options.queryBlocksOption = queryBlocksOption.UNKNOWS;
                            break;
                    }
                    Options.queryBlocksOptionSpecified = true;
                }

                Options.targetList = ProcessTargetList();

                var ActionTask = SapiClients.client.backupAsync(Options);

                var results = Progress("Backup", ActionTask);
                int index = 0;
                var returnResult = new SapiBackupResult[results.Length];
                foreach (var result in results)
                {
                    returnResult[index] = new SapiBackupResult(result);

                    if (result is resultActionBackup)
                    {
                        returnResult[index].BackupMode = (result as resultActionBackup).backupMode;

                        returnResult[index].GenerationId = (result as resultActionBackup).generationId;

                        if (returnResult[index].BackupMode == backupMode.INCREMENTAL)
                            returnResult[index].DependOnGenerationId = (result as resultActionBackup).generationInfo.previousGenerationId;
                    }
                    returnResult[index].Items = Item(index);
                    ++index;

                }
                WriteObject(returnResult, true); // This is what actually "returns" output. 
            }
        }

    }
}
