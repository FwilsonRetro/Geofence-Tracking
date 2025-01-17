import { WebPlugin } from '@capacitor/core';
import type { GeofenceBackgroundTrackingPlugin } from './definitions';
export declare class GeofenceBackgroundTrackingWeb extends WebPlugin implements GeofenceBackgroundTrackingPlugin {
    echo(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
    initializeGeofences(): Promise<void>;
}
declare const GeofenceBackgroundTrackingPlugin: GeofenceBackgroundTrackingWeb;
export { GeofenceBackgroundTrackingPlugin };
