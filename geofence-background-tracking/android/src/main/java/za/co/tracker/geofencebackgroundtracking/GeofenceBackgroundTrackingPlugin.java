package za.co.tracker.geofencebackgroundtracking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@CapacitorPlugin(name = "GeofenceBackgroundTracking", permissions = {
        @Permission(alias = "location", strings = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}),
        @Permission(alias = "backgroundLocation", strings = {Manifest.permission.ACCESS_BACKGROUND_LOCATION})
})

//PERMISSION HANDLER
public class GeofenceBackgroundTrackingPlugin extends Plugin {
    private String apiURL;
    private static final OkHttpClient client = new OkHttpClient();

    @Override
    public void load(){
        super.load();
        apiURL =  getConfig().getString("payloadURL", null);
    }

    @PluginMethod
    public void initializeGeofences(PluginCall call) {
        getBridge().saveCall(call);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "geofence_channel",
                    "Geofence Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getContext().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (getPermissionState("location") != PermissionState.GRANTED ||
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                                getPermissionState("backgroundLocation") != PermissionState.GRANTED))) {

            if (getPermissionState("backgroundLocation") == PermissionState.PROMPT_WITH_RATIONALE) {
                showPermissionRationale(call);
            } else {
                requestPermissionForAliases(new String[]{"location", "backgroundLocation"}, call, "handlePermissionResult");
            }
        } else {
            fetchCurrentLocationAndSetupGeofence(call);
        }
    }

    @PermissionCallback
    private void handlePermissionResult(PluginCall call) {
        PluginCall savedCall = getBridge().getSavedCall(call.getCallbackId());

        if (savedCall == null) {
            Log.e("GeofencingPlugin", "No saved call found.");
            return;
        }

        if (getPermissionState("location") == PermissionState.GRANTED &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                        getPermissionState("backgroundLocation") == PermissionState.GRANTED)) {
            fetchCurrentLocationAndSetupGeofence(savedCall);
        } else {
            savedCall.reject("Required location permissions are not granted.");
        }
    }

    private void showPermissionRationale(PluginCall call) {
        new AlertDialog.Builder(getContext())
                .setTitle("Background Location Required")
                .setMessage("This app needs background location access to monitor geofences and provide accurate location updates even when the app is not in use.")
                .setPositiveButton("OK", (dialog, which) -> {
                    PluginCall savedCall = getBridge().getSavedCall(call.getCallbackId());
                    Log.i("showPermissionRationale", "permissionsssss" + savedCall);
                    if (savedCall != null) {
                        requestPermissionForAliases(new String[]{"location", "backgroundLocation"}, savedCall, "handlePermissionResult");
                    } else {
                        Log.e("GeofencingPlugin", "No saved call found after rationale dialog.");
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    PluginCall savedCall = getBridge().getSavedCall(call.getCallbackId());
                    if (savedCall != null) {
                        savedCall.reject("Background location permission denied.");
                    }
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    @SuppressLint("MissingPermission")
    public void fetchCurrentLocationAndSetupGeofence(PluginCall call) {

        PowerManager powerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NEW WAKE LOCK");
        wl.acquire(15*60*1000L /*15 minutes*/);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            call.reject("Location permission not granted.");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1);
            }
        }

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                JSONObject locationPoints = new JSONObject();
                JSONObject jsonBody = new JSONObject();
                try {
                    locationPoints.put("latitude", latitude);
                    locationPoints.put("longitude", longitude);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                try {
                    String android_id = Settings.Secure.getString(getContext().getContentResolver(),
                            Settings.Secure.ANDROID_ID);
                    jsonBody.put("identifier", android_id);
                    jsonBody.put("location", locationPoints);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }


                sendPostRequest(apiURL,jsonBody.toString());

                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                        && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                    call.reject("Required location permissions are not granted.");
                }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(!powerManager.isIgnoringBatteryOptimizations(getContext().getPackageName())){
                        Intent batteryIntent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        batteryIntent.setData(Uri.parse("package:" + getContext().getPackageName()));
                        getContext().startActivity(batteryIntent);
                    } else{
                        createGeofence(getContext(),latitude,longitude);
                        call.resolve();
                    }
                }else{
                    createGeofence(getContext(),latitude,longitude);
                    call.resolve();
                }
            }
        });

    }

    public void sendPostRequest(String urlString, String jsonBody) {

        try{
            new Thread(() -> {
                RequestBody body = RequestBody.create(
                        jsonBody,
                        MediaType.get("application/json; charset=utf-8")
                );
                Request request = new Request.Builder()
                        .url(urlString)
                        .post(body)
                        .addHeader("Content-Type", "application/json")  // Set Content-Type header
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @SuppressLint("MissingPermission")
    public static void createGeofence(Context context,double latitude,double longitude) {

        Intent serviceIntent = new Intent(context, GeofenceForegroundService.class);
        serviceIntent.putExtra("latitude",latitude);
        serviceIntent.putExtra("longitude",longitude);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        }
    }
}
