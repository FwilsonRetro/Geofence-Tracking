import { PluginListenerHandle, WebPlugin } from '@capacitor/core';
import type { GeofenceBackgroundTrackingPlugin } from './definitions';
export declare class GeofenceBackgroundTrackingWeb extends WebPlugin implements GeofenceBackgroundTrackingPlugin {
    echo(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
    initializeGeofences(): Promise<void>;
    addListener(eventName: 'onEnter' | 'onExit', _listenerFunc: (data: {
        identifier: string;
    }) => void): Promise<PluginListenerHandle>;
}
declare const GeofenceBackgroundTrackingPlugin: GeofenceBackgroundTrackingWeb;
export { GeofenceBackgroundTrackingPlugin };
