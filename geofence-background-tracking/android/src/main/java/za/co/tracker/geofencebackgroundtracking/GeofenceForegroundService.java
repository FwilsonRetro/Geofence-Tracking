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

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.List;

//ACTUAL WORK/REGISTERING INTENT
public class GeofenceForegroundService extends Service {

    private static final String CHANNEL_ID = "GeofenceServiceChannel";
    private ActivityTransitionReceiver activityTransitionReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("LocationService", "Service created");
        activityTransitionReceiver = new ActivityTransitionReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Context context = getApplicationContext();
        registerActivityIntent(context);
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
                    NotificationManager.IMPORTANCE_UNSPECIFIED
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

    @SuppressLint("MissingPermission")
    private void registerActivityIntent(Context context){
        ActivityRecognitionClient activityRecognitionClient = ActivityRecognition.getClient(context);
        List<ActivityTransition> transitions = new ArrayList<>();
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());
        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);

        Intent intent = new Intent(context, ActivityTransitionReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );

        activityRecognitionClient
                .requestActivityTransitionUpdates(request, pendingIntent)
                .addOnSuccessListener(aVoid -> Log.i("ACtivity Transition", "Activity Registered successfully "))
                .addOnFailureListener(e -> Log.i("ACtivity Transition", "Activity registration failed "));

        sendBroadcast(intent);
    }
}
