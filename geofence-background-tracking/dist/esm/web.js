import { WebPlugin } from '@capacitor/core';
export class GeofenceBackgroundTrackingWeb extends WebPlugin {
    async echo(options) {
        console.log('ECHO', options);
        return options;
    }
    async initializeGeofences() {
        console.log('This is a web fallback, no geofencing support on web.');
    }
}
const GeofenceBackgroundTrackingPlugin = new GeofenceBackgroundTrackingWeb();
//# sourceMappingURL=web.js.map