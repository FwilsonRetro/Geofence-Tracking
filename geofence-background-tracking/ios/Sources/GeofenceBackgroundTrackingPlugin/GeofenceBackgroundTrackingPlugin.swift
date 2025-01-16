import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(GeofenceBackgroundTrackingPlugin)
public class GeofenceBackgroundTrackingPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "GeofenceBackgroundTrackingPlugin"
    public let jsName = "GeofenceBackgroundTracking"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "echo", returnType: CAPPluginReturnPromise)
    ]
    private let implementation = GeofenceBackgroundTracking()

    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.resolve([
            "value": implementation.echo(value)
        ])
    }
}
