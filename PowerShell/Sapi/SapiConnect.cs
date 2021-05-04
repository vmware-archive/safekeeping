using System.Security.Cryptography.X509Certificates;
using System.Net.Security;
using System.Net;
using System.ServiceModel;
using System.Management.Automation;
namespace SapiCli
{
    [Cmdlet(VerbsCommunications.Disconnect, "Sapi")]
    [OutputType(typeof(SapiDisconnectResult))]
    public class SapiDisconnect : CmdletTaskProgress
    {

        protected override void ProcessRecord()
        {
            base.ProcessRecord();


            var ActionTask = SapiClients.client.disconnectAsync();
            var ra = Progress("Disconnect", ActionTask);
            if (ra.state == operationState.SUCCESS)
            {
                MyScheduler.Stop();
                var raDisconnectSSo = SapiClients.client.logout();
                var result = new SapiDisconnectResult(raDisconnectSSo);

                WriteObject(result);// This is what actually "returns" output.
            }
            else
            {
                throw new SapiException(ra);

            }

        }
    }




    [Cmdlet(VerbsCommunications.Connect, "Sapi")]
    [OutputType(typeof(SapiConnectResult))]
    public class SapiConnect : CmdletTaskProgress
    {
        private int PortValue;
        private bool PortSet;
        #region Properties


        [Parameter(Mandatory = true)]
        [ValidateNotNullOrEmpty()]
        public string SapiUrl { get; set; }

        [Parameter(Mandatory = true)]
        [ValidateNotNullOrEmpty()]
        public string Server { get; set; }

        [Parameter(Mandatory = true)]
        [ValidateNotNullOrEmpty()]
        public string User { get; set; }

        [Parameter(Mandatory = true)]
        [ValidateNotNullOrEmpty()]
        public string Password { get; set; }


        [Parameter(Mandatory = false, ValueFromPipelineByPropertyName = false)]

        public SwitchParameter Base64 { get; set; }


        [Parameter(Mandatory = false, ValueFromPipelineByPropertyName = false, HelpMessage = "Ignore the server certificate validation")]

        public SwitchParameter IgnoreCert { get; set; }

        //    [Parameter(Mandatory = false, ValueFromPipelineByPropertyName = false, HelpMessage = "Manage vCenter extension")]


        [Parameter(Mandatory = false)]
        public int Port
        {
            get
            {
                return PortValue;
            }
            set
            {
                PortSet = true;
                PortValue = value;
            }
        }
        #endregion Properties
        private static bool ValidateRemoteCertificate(object sender, X509Certificate certificate, X509Chain chain, SslPolicyErrors policyErrors)
        {
            return true;
        }
        protected override void BeginProcessing()
        {
            if (IgnoreCert)
            {
                ServicePointManager.ServerCertificateValidationCallback += new RemoteCertificateValidationCallback(ValidateRemoteCertificate);

            }
        }

        protected override void ProcessRecord()
        {
            base.ProcessRecord();



            var remoteAddress = new System.ServiceModel.EndpointAddress(SapiUrl);
            HttpBindingBase binding;
            if (remoteAddress.Uri.Scheme == "https")
                binding = new System.ServiceModel.BasicHttpsBinding();
            else
                binding = new System.ServiceModel.BasicHttpBinding();
            SapiClients.client = new SapiClient(binding, remoteAddress);
            var behaviour = new CustomAuthenticationBehaviour();
            SapiClients.client.Endpoint.EndpointBehaviors.Add(behaviour);

            pscConnectOptions pscOptions = new pscConnectOptions
            {
                base64 = Base64,
                authServer = Server,
                user = User,
                password = Password

            };
            if (PortSet)
            {
                pscOptions.portSpecified = true;
                pscOptions.port = PortValue;
            }

            var loginResult = SapiClients.client.login(pscOptions);
            behaviour.ApplyAuthenticationToken(loginResult.token);
            var actionTask = SapiClients.client.connectAsync();

            var result = Progress("Connect", actionTask);
            var finalResult = new SapiConnectResult(result);

            // For Interval in Seconds 
            // This Scheduler will start after 0 hour and 15 minutes call after every 15 minutes
            // IntervalInSeconds(start_hour, start_minute, seconds)
            MyScheduler.IntervalInMinutes(0, 15, 15,
            () =>
            {
                SapiClients.client.keepalive();
            });
            WriteObject(finalResult); // This is what actually "returns" output.
        }

    }

}
