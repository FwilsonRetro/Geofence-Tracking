import { registerPlugin } from '@capacitor/core';

import type { GeofenceBackgroundTrackingPlugin } from './definitions';

const GeofenceBackgroundTracking = registerPlugin<GeofenceBackgroundTrackingPlugin>('GeofenceBackgroundTracking', {
  web: () => import('./web').then((m) => new m.GeofenceBackgroundTrackingWeb()),
});

export * from './definitions';
export { GeofenceBackgroundTracking };
