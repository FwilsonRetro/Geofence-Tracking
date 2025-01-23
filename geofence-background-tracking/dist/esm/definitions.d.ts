import { PluginListenerHandle } from "@capacitor/core";
export interface GeofenceBackgroundTrackingPlugin {
    echo(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
    initializeGeofences(): Promise<void>;
    addListener(eventName: 'onEnter' | 'onExit', listenerFunc: (data: {
        identifier: string;
    }) => void): Promise<PluginListenerHandle>;
}
