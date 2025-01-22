import { PluginListenerHandle } from "@capacitor/core";

export interface GeofenceBackgroundTrackingPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  initializeGeofences(): Promise<void>;
  addListener(
    eventName: 'perissionCheck',
    listenerFunc: (data: { latitude: number; longitude: number; identifier: string }) => void
  ): Promise<PluginListenerHandle>;
}
