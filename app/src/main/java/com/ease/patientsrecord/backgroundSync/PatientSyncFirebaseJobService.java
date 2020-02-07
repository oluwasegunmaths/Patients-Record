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
import android.os.AsyncTask;
import android.util.Log;

import com.ease.patientsrecord.AppExecutors;
import com.ease.patientsrecord.data.AppDatabase;
import com.ease.patientsrecord.data.Patient;
import com.ease.patientsrecord.data.PatientDao;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.RetryStrategy;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class PatientSyncFirebaseJobService extends JobService {

    private AsyncTask mBackgroundTask;


    @Override
    public boolean onStartJob(final JobParameters jobParameters) {

        mBackgroundTask = new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {

                Context context = PatientSyncFirebaseJobService.this;
                PatientDao patientDao = AppDatabase.getInstance(context).patientDao();

                List<Patient> unOploadedPatients = patientDao.getUnoploadedItems(false);
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                if (firebaseUser != null && !unOploadedPatients.isEmpty()) {
                    FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
                    mFirebaseDatabase.goOnline();
                    for (Patient patient : unOploadedPatients) {
                        if (patient.getFirebaseKey().isEmpty()) {
                            DatabaseReference patientDatabaseReference = mFirebaseDatabase.getReference().child("patients").child(firebaseUser.getUid()).push();
                            String firebaseKey = patientDatabaseReference.getKey();
                            Task<Void> task = patientDatabaseReference.setValue(patient);

                            task.addOnSuccessListener(aVoid -> {

                                AppExecutors.getInstance().diskIO().execute(() -> {
                                    Log.i("cccccccc", "0");
                                    int l = patientDao.syncDatabaseToShowPatientWithIdIsOnServer(true, firebaseKey, patient.getId());
                                    Log.i("cccccccc", "1");

                                });

                            });
                        } else {
                            DatabaseReference patientDatabaseReference = mFirebaseDatabase.getReference().child("patients").child(firebaseUser.getUid()).child(patient.getFirebaseKey());
                            Task<Void> task = patientDatabaseReference.setValue(patient);

                            task.addOnSuccessListener(aVoid -> {
                                AppExecutors.getInstance().diskIO().execute(() -> {
                                    Log.i("cccccccc", "2");

                                    patientDao.syncDatabaseToShowPatientWithIdIsOnServer(true, patient.getId());
                                    Log.i("cccccccc", "3");

                                });

                            });
                        }
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {

                jobFinished(jobParameters, false);
            }
        };

        mBackgroundTask.execute();
        return true;
    }

    /**
     * Called when the scheduling engine has decided to interrupt the execution of a running job,
     * most likely because the runtime constraints associated with the job are no longer satisfied.
     *
     * @return whether the job should be retried
     * @see Job.Builder#setRetryStrategy(RetryStrategy)
     * @see RetryStrategy
     */
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (mBackgroundTask != null) mBackgroundTask.cancel(true);
        return true;
    }
}