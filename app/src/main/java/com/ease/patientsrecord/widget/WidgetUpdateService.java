package com.ease.patientsrecord.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ease.patientsrecord.R;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class WidgetUpdateService extends IntentService {
    public static final String PATIENT_DETAILS = "patient details";
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.ease.bakingapp.action.FOO";
    private static final String EXTRA_PARAM1 = "com.ease.bakingapp.extra.PARAM1";

    public WidgetUpdateService() {
        super("WidgetUpdateService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdatePatientDetails(Context context, String param1) {
        Intent intent = new Intent(context, WidgetUpdateService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        context.startService(intent);
    }

    public static void startActionUpdatePatientDetails(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String patientDetails = preferences.getString(PATIENT_DETAILS, "");
        String widgetText;
        if (patientDetails.isEmpty()) {
            widgetText = context.getString(R.string.nothing_yet);
        } else {
            widgetText = patientDetails;

        }


        Intent intent = new Intent(context, WidgetUpdateService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, widgetText);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                handleActionFoo(param1);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String widgetText) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, PatientDetailWidget.class));

        PatientDetailWidget.updateAppWidgets(this, appWidgetManager, appWidgetIds, widgetText);

    }


}
