import SwiftUI
import Shared
import UserNotifications

@main
struct iOSApp: App {
    init() {
        DIKt.startKoinForIos()
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { _, _ in }
        NotificationScheduler.shared.start() // build the reminder window + re-arm on any change
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
