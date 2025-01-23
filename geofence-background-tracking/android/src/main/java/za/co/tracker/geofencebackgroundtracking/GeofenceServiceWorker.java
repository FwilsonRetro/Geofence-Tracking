package za.co.tracker.geofencebackgroundtracking;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
public class GeofenceServiceWorker extends Worker{

    public GeofenceServiceWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i("DO WORK", "DO WORK");
        Intent serviceIntent = new Intent(getApplicationContext(), GeofenceForegroundService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            getApplicationContext().startForegroundService(serviceIntent);
        }
        // Return success
        return Result.success();
    }

}
