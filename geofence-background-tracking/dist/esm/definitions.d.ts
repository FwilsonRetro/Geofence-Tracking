export interface GeofenceBackgroundTrackingPlugin {
    echo(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
    initializeGeofences(): Promise<void>;
}
