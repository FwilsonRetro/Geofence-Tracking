import { registerPlugin, WebPlugin } from '@capacitor/core';

import type { GeofenceBackgroundTrackingPlugin } from './definitions';

export class GeofenceBackgroundTrackingWeb extends WebPlugin implements GeofenceBackgroundTrackingPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  async initializeGeofences(): Promise<void> {

  }
}

const GeofenceBackgroundTrackingPlugin = new GeofenceBackgroundTrackingWeb();

export { GeofenceBackgroundTrackingPlugin };

import { RegisterPlugin } from '@capacitor/core/types/definitions';
registerPlugin('GeofenceBackgroundTrackingPlugin');