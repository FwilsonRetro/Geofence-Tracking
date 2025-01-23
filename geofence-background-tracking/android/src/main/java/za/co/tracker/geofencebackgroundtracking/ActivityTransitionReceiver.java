package za.co.tracker.geofencebackgroundtracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

//TRACKING LOGIC/API CALL HERE PROBABLY
public class ActivityTransitionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
        Log.i("RESULT", "RESULT: " + result);
        if (result != null) {
            List<ActivityTransitionEvent> events = result.getTransitionEvents();
            for (ActivityTransitionEvent event : events) {
                int activityType = event.getActivityType();
                int transitionType = event.getTransitionType();

                String activityName = getActivityName(activityType);
                String transitionName = getTransitionName(transitionType);

                Log.d("TAG", "Activity: " + activityName + ", Transition: " + transitionName);

                //Stops service
                if (activityType == DetectedActivity.WALKING &&
                        transitionType == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
                    Intent stopServiceIntent = new Intent(context, GeofenceForegroundService.class);
                    context.stopService(stopServiceIntent);

                    PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                            GeofenceServiceWorker.class,
                            15, TimeUnit.MINUTES
                    ).build();

                    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                            "GeofenceServiceWorker",
                            ExistingPeriodicWorkPolicy.UPDATE,
                            workRequest
                    );
                    Log.d("TAG", "Foreground service stopped after walking exited.");
                }
            }
        }

    }

    private String getActivityName(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "In Vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "On Bicycle";
            case DetectedActivity.ON_FOOT:
                return "On Foot";
            case DetectedActivity.RUNNING:
                return "Running";
            case DetectedActivity.STILL:
                return "Still";
            case DetectedActivity.TILTING:
                return "Tilting";
            case DetectedActivity.WALKING:
                return "Walking";
            case DetectedActivity.UNKNOWN:
            default:
                return "Unknown";
        }
    }

    private String getTransitionName(int transitionType) {
        switch (transitionType) {
            case ActivityTransition.ACTIVITY_TRANSITION_ENTER:
                return "Entered";
            case ActivityTransition.ACTIVITY_TRANSITION_EXIT:
                return "Exited";
            default:
                return "Unknown Transition";
        }
    }
}
