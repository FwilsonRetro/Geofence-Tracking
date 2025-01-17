import { WebPlugin } from '@capacitor/core';

import type { GeofenceBackgroundTrackingPlugin } from './definitions';

export class GeofenceBackgroundTrackingWeb extends WebPlugin implements GeofenceBackgroundTrackingPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  async initializeGeofences(): Promise<void> {
    console.log('This is a web fallback, no geofencing support on web.');
  }
}

const GeofenceBackgroundTrackingPlugin = new GeofenceBackgroundTrackingWeb();

export { GeofenceBackgroundTrackingPlugin };