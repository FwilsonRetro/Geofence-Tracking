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
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

@CapacitorPlugin(name = "GeofenceBackgroundTracking", permissions = {
        @Permission(alias = "location", strings = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}),
        @Permission(alias = "backgroundLocation", strings = {Manifest.permission.ACCESS_BACKGROUND_LOCATION})
})
public class GeofenceBackgroundTrackingPlugin extends Plugin {

    private static GeofencingClient getGeofencingClient(Context context) {
        return LocationServices.getGeofencingClient(context.getApplicationContext());
    }

    @PluginMethod
    public void initializeGeofencing(PluginCall call) {
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
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            call.reject("Location permission not granted.");
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                Log.i("GeofencingPlugin", "Fetched location: " + latitude + ", " + longitude);
                Log.e("GeofencingPlugin", "Location Permission State: " + getPermissionState("location"));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Log.e("GeofencingPlugin", "Background Location Permission State: " + getPermissionState("backgroundLocation"));
                }
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                        && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                    call.reject("Required location permissions are not granted.");
                }else {
                    createGeofence(latitude, longitude,getContext());
                    call.resolve(new JSObject().put("message", "Geofencing initialized at: " + latitude + ", " + longitude));
                }
            } else {
                call.reject("Unable to fetch location. Ensure GPS is enabled.");
            }
        }).addOnFailureListener(e -> {
            call.reject("Error fetching location: " + e.getMessage());
        });
    }

    @SuppressLint("MissingPermission")
    public static void createGeofence(double latitude, double longitude, Context context) {
        GeofencingClient geofencingClient = getGeofencingClient(context);
        Geofence geofence = new Geofence.Builder()
                .setRequestId("InitialGeofence")
                .setCircularRegion(latitude, longitude, 100.0f)
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
    }
}
