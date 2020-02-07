/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ease.patientsrecord.backgroundSync;

import android.content.Context;

import androidx.annotation.NonNull;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

public class SyncUtilities {
    /*
     * Interval at which to remind the user to drink water. Use TimeUnit for convenience, rather
     * than writing out a bunch of multiplication ourselves and risk making a silly mistake.
     */
    private static final int INTERVAL_MINUTES = 15;
    private static final int INTERVAL_SECONDS = (int) (TimeUnit.MINUTES.toSeconds(INTERVAL_MINUTES));
    private static final int SYNC_FLEXTIME_SECONDS = INTERVAL_SECONDS;

    private static final String TAG = "sync_patients_tag";

    private static boolean sInitialized;

    synchronized public static void schedulePatientSync(@NonNull final Context context) {
        if (sInitialized) return;

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        Job constraintReminderJob = dispatcher.newJobBuilder().setService(PatientSyncFirebaseJobService.class)
                /*
                 * Set the UNIQUE tag used to identify this Job.
                 */.setTag(TAG)

//                .setConstraints(Constraint.DEVICE_CHARGING)

                .setConstraints(Constraint.ON_ANY_NETWORK)

                .setLifetime(Lifetime.FOREVER)

                .setRecurring(true)

                .setTrigger(Trigger.executionWindow(0, 30))

                .setReplaceCurrent(true).build();

        /* Schedule the Job with the dispatcher */

        dispatcher.mustSchedule(constraintReminderJob);

        /* The job has been initialized */
        sInitialized = true;
    }

}
