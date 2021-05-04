using System.Management.Automation;
using System;

namespace SapiCli
{

    [Cmdlet(VerbsCommunications.Connect, "Repository")]
    [OutputType(typeof(resultAction))]
    public class SapiConnectRepository : CmdletTaskProgress
    {
        #region Properties
        [Parameter(Mandatory = true)]
        [ValidateNotNullOrEmpty()]
        public string Name { get; set; }

        [Parameter(Mandatory = false)]
        public SwitchParameter Default { get; set; }


        [Parameter(Mandatory = true, ParameterSetName = "S3")]
        public SwitchParameter S3 { get; set; }

        [Parameter(Mandatory = true, ParameterSetName = "S3")]
        [ValidateNotNullOrEmpty()]
        public string Backet { get; set; }

        [Parameter(Mandatory = true, ParameterSetName = "S3")]
        [ValidateNotNullOrEmpty()]
        public string Region { get; set; }

        [Parameter(Mandatory = true, ParameterSetName = "S3")]
        [ValidateNotNullOrEmpty()]
        public string AccessKey { get; set; }

        [Parameter(Mandatory = true, ParameterSetName = "S3")]
        [ValidateNotNullOrEmpty()]
        public string SecretKey { get; set; }

        [Parameter(Mandatory = false, ParameterSetName = "S3")]
        public SwitchParameter Base64 { get; set; }







        [Parameter(Mandatory = true, ParameterSetName = "File")]
        public SwitchParameter File { get; set; }

        [Parameter(Mandatory = true, ParameterSetName = "File")]
        [ValidateNotNullOrEmpty()]
        public string RootFolder { get; set; }

        #endregion Properties

        protected override void ProcessRecord()
        {
            base.ProcessRecord();
            repositoryOptions Options = null;
            SapiClients.CheckConnection();
            if (S3)
            {
                Options = new awsS3RepositoryOptions()
                {
                    accessKey = AccessKey,
                    backet = Backet,
                    name = Name,
                    region = Region,
                    base64 = Base64,
                    secretKey = SecretKey,
                    active = Default
                };
            }
            else if (File)
            {
                Options = new fileRepositoryOptions()
                {
                    name = Name,
                    rootFolder = RootFolder,
                    active = Default
                };
            }

            var actionTask = SapiClients.client.connectRepositoryAsync(Options);
            var result = Progress("ConnectRepository", actionTask);
            var finalResult = new SapiConnectRepositoryResult(result);
            WriteObject(finalResult); // This is what actually "returns" output. 
        }

    }




    [Cmdlet(VerbsCommunications.Disconnect, "Repository")]
    [OutputType(typeof(resultAction))]
    public class SapiDisconnectRepository : CmdletTaskProgress
    {
        [Parameter(Position = 0, Mandatory = true, ValueFromPipeline = true, ValueFromPipelineByPropertyName = true)]
        [ValidateNotNullOrEmpty()]
        public string Name { get; set; }





        protected override void ProcessRecord()
        {
            base.ProcessRecord();
            SapiClients.CheckConnection();
            var actionTask = SapiClients.client.disconnectRepositoryAsync(Name);


            var result = Progress("ConnectRepository", actionTask);

            WriteObject(result); // This is what actually "returns" output.
        }

    }

    [Cmdlet(VerbsCommon.Get, "Repository")]
    [OutputType(typeof(resultAction))]
    public class SapiGetRepository : CmdletTaskProgress
    {
        [Parameter(Position = 0, Mandatory = false, ValueFromPipeline = true, ValueFromPipelineByPropertyName = true)]
        [ValidateNotNullOrEmpty()]
        public string Name { get; set; }



        protected override void ProcessRecord()
        {
            base.ProcessRecord();
            SapiClients.CheckConnection();
            if (Name != null)
            {
                var target = SapiClients.client.getRepository(Name);
                WriteObject(target); // This is what actually "returns" output.
            }
            else
            {
                var targets = SapiClients.client.getRepositories();
                WriteObject(targets); // This is what actually "returns" output.
            }
        }

    }




    [Cmdlet(VerbsCommon.Set, "ActiveRepository")]
    [OutputType(typeof(resultAction))]
    public class SapiSetActiveRepository : CmdletTaskProgress
    {
        [Parameter(Position = 0, Mandatory = true, ValueFromPipeline = true, ValueFromPipelineByPropertyName = true)]
        [ValidateNotNullOrEmpty()]
        public string Name { get; set; } 

        protected override void ProcessRecord()
        {
            base.ProcessRecord();
            SapiClients.CheckConnection();
            var target = SapiClients.client.setActiveRepository(Name);
            Boolean result = target.state == operationState.SUCCESS;
            WriteObject(result,true); // This is what actually "returns" output. 
        }

    }

    [Cmdlet(VerbsCommon.Get, "ActiveRepository")]
    [OutputType(typeof(resultAction))]
    public class SapiGetActiveRepository : CmdletTaskProgress
    {
      
        protected override void ProcessRecord()
        {
            base.ProcessRecord();
            SapiClients.CheckConnection();
            var result = SapiClients.client.getActiveRepository( );
        
            WriteObject(result); // This is what actually "returns" output. 
        }

    }
}
