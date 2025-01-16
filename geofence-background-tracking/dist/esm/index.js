import { registerPlugin } from '@capacitor/core';
const GeofenceBackgroundTracking = registerPlugin('GeofenceBackgroundTracking', {
    web: () => import('./web').then((m) => new m.GeofenceBackgroundTrackingWeb()),
});
export * from './definitions';
export { GeofenceBackgroundTracking };
//# sourceMappingURL=index.js.map