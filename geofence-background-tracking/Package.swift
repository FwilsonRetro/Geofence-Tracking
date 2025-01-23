// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "GeofenceBackgroundTracking",
    platforms: [.iOS(.v13)],
    products: [
        .library(
            name: "GeofenceBackgroundTracking",
            targets: ["GeofenceBackgroundTrackingPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", branch: "main")
    ],
    targets: [
        .target(
            name: "GeofenceBackgroundTrackingPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/GeofenceBackgroundTrackingPlugin"),
        .testTarget(
            name: "GeofenceBackgroundTrackingPluginTests",
            dependencies: ["GeofenceBackgroundTrackingPlugin"],
            path: "ios/Tests/GeofenceBackgroundTrackingPluginTests")
    ]
)