'use strict';

var core = require('@capacitor/core');

const GeofenceBackgroundTracking = core.registerPlugin('GeofenceBackgroundTracking', {
    web: () => Promise.resolve().then(function () { return web; }).then((m) => new m.GeofenceBackgroundTrackingWeb()),
});

class GeofenceBackgroundTrackingWeb extends core.WebPlugin {
    async echo(options) {
        console.log('ECHO', options);
        return options;
    }
}

var web = /*#__PURE__*/Object.freeze({
    __proto__: null,
    GeofenceBackgroundTrackingWeb: GeofenceBackgroundTrackingWeb
});

exports.GeofenceBackgroundTracking = GeofenceBackgroundTracking;
//# sourceMappingURL=plugin.cjs.js.map
