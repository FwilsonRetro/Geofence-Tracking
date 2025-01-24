package za.co.tracker.geofencebackgroundtracking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//ACTUAL WORK/REGISTERING INTENT
public class GeofenceForegroundService extends Service {

    private static final String CHANNEL_ID = "GeofenceServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("LocationService", "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("FOREGROUND", "Foreground started");
        Context context = getApplicationContext();
        double latitude = intent.getDoubleExtra("latitude",0.0);
        double longitude = intent.getDoubleExtra("longitude",0.0);
            registerGeofenceIntent(context,latitude,longitude);
            createNotificationChannel();
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setPriority(Notification.PRIORITY_MIN)
                    .setContentTitle("Location Service")
                    .build();
            startForeground(1, notification);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("LocationService", "Service destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_NONE
            );
        }

        NotificationManager manager = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager = getSystemService(NotificationManager.class);
        }
        if (manager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private static GeofencingClient getGeofencingClient(Context context) {
        return LocationServices.getGeofencingClient(context.getApplicationContext());
    }

    @SuppressLint("MissingPermission")
    private void registerGeofenceIntent(Context context,double latitude,double longitude){

        GeofencingClient geofencingClient = getGeofencingClient(context);

        Geofence geofence = new Geofence.Builder()
                .setRequestId("InitialGeofence")
                .setCircularRegion(latitude, longitude, 10.0f)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .build();

        Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);

        PendingIntent geofencePendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            geofencePendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            geofencePendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnSuccessListener(aVoid -> Log.i("GeofencingPlugin", "Geofence added successfully."))
                .addOnFailureListener(e -> {
                    Log.e("GeofencingPlugin", "Failed to add geofence: " + e.getMessage());
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        Log.e("GeofencingPlugin", "Error code: " + apiException.getStatusCode());
                    }
                });
        sendBroadcast(intent);

    }
}
