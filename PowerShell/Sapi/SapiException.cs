using System; 
namespace SapiCli
{
    //Creating our own Exception Class by inheriting Exception class
    [Serializable]
    public class SapiException : Exception
    {
        [NonSerialized]
        readonly String _message;
        public SapiException(resultAction ra)
        {
            _message = ra.reason;
        }
        public SapiException(String msg  )
        {
             _message = msg;
        }

        //Overriding the Message property
        public override string Message
        {
            get
            {
                return _message;
            }
        }
    }
}
