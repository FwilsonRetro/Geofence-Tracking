import { WebPlugin } from '@capacitor/core';
export class GeofenceBackgroundTrackingWeb extends WebPlugin {
    async echo(options) {
        console.log('ECHO', options);
        return options;
    }
    async initializeGeofences() {
        console.log('This is a web fallback, no geofencing support on web.');
    }
    async addListener(eventName, _listenerFunc) {
        console.warn(`Listening for '${eventName}' is not supported on the web.`);
        return Promise.resolve({
            remove: async () => {
                console.warn(`Removed listener for '${eventName}' on the web.`);
            },
        });
    }
}
const GeofenceBackgroundTrackingPlugin = new GeofenceBackgroundTrackingWeb();
//# sourceMappingURL=web.js.map