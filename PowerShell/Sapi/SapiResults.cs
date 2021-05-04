using System; 
using VMware.VimAutomation.ViCore.Types.V1.Inventory;

namespace SapiCli
{
    public class SapiConnectResult : SapiResult
    {
        public SapiConnectResult(resultAction result) : base(result) { }
        public String[] VIServer { get; internal set; }

    }
    public class SapiDisconnectResult : SapiResult
    {
        public SapiDisconnectResult(resultAction result) : base(result) { }
    }
    public class SapiRestoreResult : SapiResult
    {
        public SapiRestoreResult(resultAction result) : base(result) { }


        public int GenerationId { get; internal set; }
        public String Item { get; internal set; }

    }


    public class SapiConnectRepositoryResult : SapiResult
    {
        public SapiConnectRepositoryResult(resultAction result) : base(result)
        {
            name = (result as abstractResultActionConnectRepository).name;
        }
        public String name { get; internal set; }
    }

    public class SapiDisconnectRepositoryResult : SapiResult
    {
        public SapiDisconnectRepositoryResult(resultAction result) : base(result)
        {
            name = (result as resultActionDisconnectRepository).name;
        }
        public String name { get; internal set; }
    }


    public abstract class SapiResult
    {
        protected SapiResult(resultAction result)
        {
            Result = result.state;
            Reason = result.reason;
            StartTime = result.startDate;
            EndTime = result.endDate;
        }

        public operationState Result { get; private set; }
        public string Reason { get; private set; }
        public DateTime StartTime { get; private set; }
        public DateTime EndTime { get; private set; }
        public TimeSpan TotalTime { get { return EndTime - StartTime; } }
    }

    public class SapiVirtualBackupResult : SapiResult
    {
        public SapiVirtualBackupResult(resultAction result) : base(result) { }
        public backupMode BackupMode { get; internal set; }
        public int GenerationId { get; internal set; }
        public InventoryItem Items { get; internal set; }

    }

    public class SapiBackupResult : SapiResult
    {
        public SapiBackupResult(resultAction result) : base(result) { }
        public backupMode BackupMode { get; internal set; }
        public int GenerationId { get; internal set; }
        public int DependOnGenerationId { get; internal set; }
        public InventoryItem Items { get; internal set; }
    }





    public class SapiArchiveListResult : SapiResult
    {
        public SapiArchiveListResult(resultAction result) : base(result)
        {
            var item = (result as resultActionArchiveItem).info;
            entityType = item.entityType;
            full = item.full;
            Moref = item.moref;
            Name = item.name;
            TimestampOfLatestGenerationId = item.timestampOfLatestGenerationId;
            TimestampOfLatestSucceededGenerationId = item.timestampOfLatestSucceededGenerationId;
            Uuid = item.uuid;
        }
        public entityType entityType { get; internal set; }
        public Boolean full { get; internal set; }
        public String Moref { get; internal set; }
        public String Name { get; internal set; }
        public String TimestampOfLatestGenerationId { get; internal set; }

        public String TimestampOfLatestSucceededGenerationId { get; internal set; }

        public String Uuid { get; internal set; }

    }
}
