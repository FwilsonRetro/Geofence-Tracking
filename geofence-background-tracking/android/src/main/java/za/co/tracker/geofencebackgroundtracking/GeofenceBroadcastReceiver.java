package za.co.tracker.geofencebackgroundtracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.getcapacitor.PluginCall;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.concurrent.TimeUnit;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent == null) {
            Log.e("GeofenceReceiver", "GeofencingEvent is null.");
            return;
        }
        if (geofencingEvent.hasError()) {
            int errorCode = geofencingEvent.getErrorCode();
            Log.e("GeofenceReceiver", "GeofencingEvent error: " + errorCode);
            return;
        }

        handleGeofenceEvent(geofencingEvent,context);
    }


    private void handleGeofenceEvent(GeofencingEvent geofencingEvent, Context context) {
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.i("GeofenceReceiver", "Geofence exited.");
            double latitude = geofencingEvent.getTriggeringLocation().getLatitude();
            double longitude = geofencingEvent.getTriggeringLocation().getLongitude();
            GeofenceBackgroundTrackingPlugin.fetchCurrentLocationAndSetupGeofence(context);
            Log.d("TAG", "Foreground service stopped after walking exited.");
        } else {
            Log.e("GeofenceReceiver", "Unknown geofence transition: " + geofenceTransition);
        }
    }
}
