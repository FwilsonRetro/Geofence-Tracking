import Foundation
import Capacitor
import CoreLocation

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(GeofenceBackgroundTrackingPlugin)
public class GeofenceBackgroundTrackingPlugin: CAPPlugin, CAPBridgedPlugin, CLLocationManagerDelegate {
    public let identifier = "GeofenceBackgroundTrackingPlugin"
    public let jsName = "GeofenceBackgroundTracking"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "echo", returnType: CAPPluginReturnPromise)
    ]
    private let locationManager = CLLocationManager();
    
    public override func load() {
            super.load()
            locationManager.delegate = self
        }

    @available(iOS 14.0, *)
    @objc func initializeGeofences(_ call: CAPPluginCall) {
            // Check the current location authorization status
            let authorizationStatus = locationManager.authorizationStatus
            
            switch authorizationStatus {
            case .notDetermined:
                // Request permissions if not determined
                locationManager.requestAlwaysAuthorization()
                call.resolve(["status": "requested permissions"])
            case .authorizedAlways:
                // Permissions granted, set up geofences
                setupGeofences()
                call.resolve(["status": "geofences initialized"])
            case .authorizedWhenInUse:
                // Request "Always" permission if only "When In Use" is granted
                locationManager.requestAlwaysAuthorization()
                call.resolve(["status": "requested always permission"])
            case .denied, .restricted:
                // Permissions denied or restricted
                call.reject("Location permissions are denied or restricted")
            default:
                call.reject("Unknown location authorization status")
            }
        }
    
        private func setupGeofences() {
            // Add logic to create initial geofences
            let exampleGeofence = CLCircularRegion(
                center: CLLocationCoordinate2D(latitude: 37.7749, longitude: -122.4194),
                radius: 100, // Radius in meters
                identifier: "example_geofence"
            )
            exampleGeofence.notifyOnExit = true
            locationManager.startMonitoring(for: exampleGeofence)
        }

        // Handle authorization changes
    @available(iOS 14.0, *)
    public func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
            switch manager.authorizationStatus {
            case .authorizedAlways:
                setupGeofences()
                notifyListeners("permissionGranted", data: ["level": "always"])
            case .denied:
                notifyListeners("permissionDenied", data: nil)
            default:
                break
            }
        }
    
}
