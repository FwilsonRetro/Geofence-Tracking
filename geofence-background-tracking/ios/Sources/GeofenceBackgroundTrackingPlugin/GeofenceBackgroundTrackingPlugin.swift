import Foundation
import Capacitor
import CoreLocation

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */

@objc(GeofenceBackgroundTrackingPlugin)
public class GeofenceBackgroundTrackingPlugin: CAPPlugin, CLLocationManagerDelegate {
    public let identifier = "GeofenceBackgroundTrackingPlugin"
    public let jsName = "GeofenceBackgroundTracking"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "echo", returnType: CAPPluginReturnPromise)
    ]
    
    private var locationManager: CLLocationManager!
    
    @objc func initializeGeofences(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.locationManager = CLLocationManager()
            self.locationManager.delegate = self  // Fix: Assign delegate correctly
            self.locationManager.requestAlwaysAuthorization()

            if CLLocationManager.isMonitoringAvailable(for: CLCircularRegion.self) {
                self.addGeofence()
                call.resolve([
                    "status": "Geofencing initialized successfully"
                ])
            } else {
                call.reject("Geofencing is not supported on this device.")
            }
        }
    }

    @available(iOS 14.0, *)
    @objc func addGeofence() {
        guard CLLocationManager.locationServicesEnabled() else {
                print("Location services are not enabled.")
                return
            }

            let authorizationStatus = locationManager.authorizationStatus
            guard authorizationStatus == .authorizedAlways || authorizationStatus == .authorizedWhenInUse else {
                print("Location permissions not granted.")
                return
            }

            // Get the current location
            if let currentLocation = locationManager.location {
                let latitude = currentLocation.coordinate.latitude
                let longitude = currentLocation.coordinate.longitude
                let radius: CLLocationDistance = 200 // Set a fixed radius

                // Use the device ID as the identifier
                let identifier = UIDevice.current.identifierForVendor?.uuidString ?? "UnknownDevice"

                let coordinate = CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
                let region = CLCircularRegion(center: coordinate, radius: radius, identifier: identifier)

                region.notifyOnEntry = true
                region.notifyOnExit = true

                locationManager.startMonitoring(for: region)

                print("Geofence added successfully with ID: \(identifier)")
            } else {
                print("Unable to get current location.")
            }
    }

    public func locationManager(_ manager: CLLocationManager, didEnterRegion region: CLRegion) {
        if let circularRegion = region as? CLCircularRegion {
            notifyListeners("onEnter", data: [
                "identifier": circularRegion.identifier
            ])
        }
    }

    public func locationManager(_ manager: CLLocationManager, didExitRegion region: CLRegion) {
        if let circularRegion = region as? CLCircularRegion {
            notifyListeners("onExit", data: [
                "identifier": circularRegion.identifier
            ])
        }
    }

    public func locationManager(_ manager: CLLocationManager, monitoringDidFailFor region: CLRegion?, withError error: Error) {
        print("Monitoring failed for region: \(String(describing: region?.identifier)), error: \(error.localizedDescription)")
    }
    
}
