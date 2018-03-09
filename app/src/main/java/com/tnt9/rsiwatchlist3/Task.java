package com.tnt9.rsiwatchlist3;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;


class Task {

    public static void scheduleJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, CustomJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(7200 * 1000); // wait at least
        builder.setOverrideDeadline(3600 * 1000); // maximum delay
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }
}
