import { PluginListenerHandle, WebPlugin } from '@capacitor/core';

import type { GeofenceBackgroundTrackingPlugin } from './definitions';

export class GeofenceBackgroundTrackingWeb extends WebPlugin implements GeofenceBackgroundTrackingPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  async initializeGeofences(): Promise<void> {
    console.log('This is a web fallback, no geofencing support on web.');
  }

  async addListener(
    eventName: 'onEnter' | 'onExit',
    _listenerFunc: (data: { identifier: string }) => void
  ): Promise<PluginListenerHandle> {
    console.warn(`Listening for '${eventName}' is not supported on the web.`);
    return Promise.resolve({
      remove: async () => {
        console.warn(`Removed listener for '${eventName}' on the web.`);
      },
    });
  }

}

const GeofenceBackgroundTrackingPlugin = new GeofenceBackgroundTrackingWeb();

export { GeofenceBackgroundTrackingPlugin };