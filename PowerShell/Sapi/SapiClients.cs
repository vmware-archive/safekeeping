
namespace SapiCli
{
    public static class SapiClients
    {
        static public SapiClient client { get; set; }


        static public void CheckConnection()
        {
            if (client == null)
                throw new SapiException("No Sapi endpoint is connected");
        }
    }

}
