using System.Management.Automation; 
using System.Threading; 
using System;  

namespace SapiCli
{
     

    abstract public class CmdletTaskProgress : Cmdlet
    {
      
        protected resultAction Progress(String title, task actionTask, int id = 0)
        {
            var taskAction = SapiClients.client.getTaskInfo(actionTask);
            var titleStr = (actionTask.fcoEntity != null) ? title + ":" + actionTask.fcoEntity.entityType.ToString() + ":" + actionTask.fcoEntity.name : title;
            ProgressRecord myprogress = new ProgressRecord(id, titleStr, "Progress:");
            while (!taskAction.result.done)
            {
                myprogress.PercentComplete = taskAction.result.progress;
                Thread.Sleep(1000);
                WriteProgress(myprogress);
                taskAction = SapiClients.client.getTaskInfo(actionTask);

            }
            myprogress.PercentComplete = 100;
            WriteProgress(myprogress);
            return taskAction.result;
        }
      
        protected resultAction[] Progress(String title, tasks actionTasks)
        {
            var TaskList = actionTasks.taskList;
             
            resultAction[] results = null;
            switch (actionTasks.state)
            { 
                case operationState.FAILED: 
                case operationState.SKIPPED: 
                case operationState.ABORTED:
                 results = new resultAction[1];
                    results[0] = new resultAction 
                    {
                        state = actionTasks.state,
                        reason = actionTasks.reason
                    };
                    break;
                case operationState.STARTED:
                case operationState.QUEUED:
                case operationState.SUCCESS:
                    int numberOfTasks = TaskList.Length;
                    results = new resultAction[numberOfTasks]; 
                    if (numberOfTasks == 1)
                    {
                        results[0] = Progress(title, TaskList[0]);
                    }
                    else
                    {
                        ProgressRecord[] myprogresses = new ProgressRecord[numberOfTasks];

                        var globalProgress = new ProgressRecord(0, title, "Progress:");
                        for (int id = 0; id < numberOfTasks; id++)
                        {
                            var titleStr = (TaskList[id].fcoEntity != null) ? title +  ":" + TaskList[id].fcoEntity.entityType.ToString() + ":" + TaskList[id].fcoEntity.name : title;

                            myprogresses[id] = new ProgressRecord(id + 1, titleStr  , "Progress:")
                            {
                                ParentActivityId = 0
                            };
                            results[id] = SapiClients.client.getTaskInfo(TaskList[id]).result;
                            Thread.Sleep(100);
                        }
                        Boolean allDone = false;

                        while (!allDone)
                        {

                            for (int id = 0; id < numberOfTasks; id++)
                            {
                                if (!results[id].done)
                                {
                                    results[id] = SapiClients.client.getTaskInfo(TaskList[id]).result;
                                    myprogresses[id].PercentComplete = results[id].progress;
                                    WriteProgress(myprogresses[id]);
                                    Thread.Sleep(1000);
                                }
                            }
                            WriteProgress(globalProgress);
                            allDone = true;
                            for (int id = 0; id < numberOfTasks; id++)
                            {
                                allDone &= results[id].done;
                            }
                        }
                    }
                    break;
            }
            return results;
        }

    }





    //svcutil http://localhost:8080/sdk?wsdl /synconly /edb
    /*
    Import-Module C:\Users\mdaneri\source\repos\Sapi\Sapi\bin\Debug\SapiCli.dll
    Connect-Sapi -SapiUrl http://localhost:8080/sdk -Server w1-eco-vcsa-01.eco.eng.vmware.com -User administrator@vsphere.local -Password '_Ca$hc0w' -Base64 
    Connect-Target -Region  "us-west-2" -Backet "vmbk4/dev" -AccessKey "AKIA3CV76DGGGU4C763N" -SecretKey "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"  -Base64  -Name "S3"
    remove-module sapiCli
    */

    // svcutil http://localhost:8080/sdk?wsdl /synconly

    /*   protected override void BeginProcessing()
       {
           if (agedynparm != null && agedynparm.Age < 21)
           {
               ParameterBindingException pbe = new ParameterBindingException("You are not old enough for Hard Lemonade. How about a nice glass of regular Lemonade instead?");
               ErrorRecord erec = new ErrorRecord(pbe, null, ErrorCategory.PermissionDenied, agedynparm.Age);
               ThrowTerminatingError(erec);
           } 

       }*/
}

