package za.co.tracker.geofencebackgroundtracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

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
