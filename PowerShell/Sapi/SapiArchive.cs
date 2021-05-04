using System;
using System.Management.Automation; 
using VMware.VimAutomation.ViCore.Types.V1.Inventory;

namespace SapiCli
{

    /*  [Cmdlet(VerbsData.Backup, "VApp")]
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
      }*/
    [Cmdlet(VerbsCommon.Get, "ARCHIVE")]
    [OutputType(typeof(SapiResult[]))]
    public class SapiGetArchive : SapiArchive
    {
        [Parameter(Position = 0, Mandatory = false, ValueFromPipeline = true, HelpMessage = "Specifies the virtual machines you want to backup.", ParameterSetName = "Backup")]
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

    }




    abstract public class SapiArchive : CmdletTaskProgress
    {
       

        abstract protected fcoTarget[] ProcessTargetList();
        protected override void ProcessRecord()
        {
            base.ProcessRecord();

            SapiClients.CheckConnection();
            var ListOptions = new archiveListOptions();
             
            var ActionTask = SapiClients.client.listArchive(ListOptions);

            var result = Progress("Archive list", ActionTask);
            var resultList = new SapiArchiveListResult[(result as resultActionArchiveItemsList).items.Length];


            for (int j = 0; j < (result as resultActionArchiveItemsList).items.Length; j++)
            {
                resultList[j] = new SapiArchiveListResult((result as resultActionArchiveItemsList).items[j]);
            }
            WriteObject(resultList, true);
        }

    }
}
