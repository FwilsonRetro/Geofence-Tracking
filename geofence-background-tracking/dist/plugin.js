var capacitorGeofenceBackgroundTracking = (function (exports, core) {
    'use strict';

    const GeofenceBackgroundTracking = core.registerPlugin('GeofenceBackgroundTracking', {
        web: () => Promise.resolve().then(function () { return web; }).then((m) => new m.GeofenceBackgroundTrackingWeb()),
    });

    class GeofenceBackgroundTrackingWeb extends core.WebPlugin {
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
    new GeofenceBackgroundTrackingWeb();

    var web = /*#__PURE__*/Object.freeze({
        __proto__: null,
        GeofenceBackgroundTrackingWeb: GeofenceBackgroundTrackingWeb
    });

    exports.GeofenceBackgroundTracking = GeofenceBackgroundTracking;

    return exports;

})({}, capacitorExports);
//# sourceMappingURL=plugin.js.map
