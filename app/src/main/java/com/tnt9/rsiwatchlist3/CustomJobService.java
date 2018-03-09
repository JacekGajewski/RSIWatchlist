package com.tnt9.rsiwatchlist3;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;


public class CustomJobService extends JobService{

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Intent service = new Intent(getApplicationContext(), CheckRsiService.class);
        if(Build.VERSION.SDK_INT >= 26){
            getApplicationContext().startForegroundService(service);
        }else getApplicationContext().startService(service);
        Task.scheduleJob(getApplicationContext()); // reschedule the job
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}
