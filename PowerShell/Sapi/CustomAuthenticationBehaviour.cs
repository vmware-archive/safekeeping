using System.Net; 
using System.ServiceModel.Dispatcher;
using System.ServiceModel.Channels;
using System.ServiceModel.Description;

namespace SapiCli
{
    public class CustomAuthenticationBehaviour : IEndpointBehavior
    {

        readonly CustomMessageInspector messageInspector;
        public CustomAuthenticationBehaviour()
        {
            messageInspector = new CustomMessageInspector();
        }


        public void ApplyAuthenticationToken(string authToken)
        {
            messageInspector.authToken = authToken;
        }
        public void Validate(ServiceEndpoint endpoint)
        {
            // Do nothing 
        }

            public void AddBindingParameters(ServiceEndpoint endpoint, BindingParameterCollection bindingParameters)
        { 
            // Do nothing 
        }

        public void ApplyDispatchBehavior(ServiceEndpoint endpoint, EndpointDispatcher endpointDispatcher)
        {
            // Do nothing 
        }

        public void ApplyClientBehavior(ServiceEndpoint endpoint, ClientRuntime clientRuntime)
        {
            clientRuntime.ClientMessageInspectors.Add(messageInspector);
        }
    }

    public class CustomMessageInspector : IClientMessageInspector
    {
        public  string  authToken { get; set; }

        public CustomMessageInspector( )
        {
             authToken = null;
        }

        public void AfterReceiveReply(ref System.ServiceModel.Channels.Message reply,
            object correlationState)
        {
            // Do nothing 
        }

            public object BeforeSendRequest(ref System.ServiceModel.Channels.Message request,
            System.ServiceModel.IClientChannel channel)
        {
            if (authToken != null)
            {
                var reqMsgProperty = new HttpRequestMessageProperty();
                reqMsgProperty.Headers.Add("Auth-token", authToken);
                request.Properties[HttpRequestMessageProperty.Name] = reqMsgProperty;
            }
            return null;
 
        }
    }
}

