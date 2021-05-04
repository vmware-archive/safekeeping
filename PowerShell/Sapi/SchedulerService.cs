namespace SapiCli
{
    using System; 
    using System.Threading;

    public class SchedulerService
    {
        private static SchedulerService _instance;
        private   Timer timer;

        private SchedulerService() { }

        public static SchedulerService Instance => _instance ?? (_instance = new SchedulerService());

        public void ScheduleTask(int hour, int min, double intervalInHour, Action task)
        {
            DateTime now = DateTime.Now;

            int day = now.Day;
            int scheduleMinute = now.Minute + min;
            if (scheduleMinute > 59)
            {
                ++hour;
                scheduleMinute -= 60;
            }

            int scheduleHour = now.Hour + hour;
            if (scheduleHour > 23)
            {
                ++day;
                scheduleHour -= 24;
            }

            DateTime firstRun = new DateTime(now.Year, now.Month, day, scheduleHour, scheduleMinute, 0, 0);
            if (now > firstRun)
            {
                firstRun = firstRun.AddDays(1);
            }

            TimeSpan timeToGo = firstRun - now;
            if (timeToGo <= TimeSpan.Zero)
            {
                timeToGo = TimeSpan.Zero;
            }

            timer = new Timer(x =>
          {
              task.Invoke();
          }, null, timeToGo, TimeSpan.FromHours(intervalInHour));

        }

        public void Stop()
        {
            if (timer != null)
            {
                timer.Dispose();
            }
        }


    }
}