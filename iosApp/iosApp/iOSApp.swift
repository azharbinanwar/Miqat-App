import SwiftUI
import Shared
import UserNotifications
import BackgroundTasks

private let notifRefreshId = "dev.miqat.notif.refresh"

// Best-effort background top-up. iOS decides when (if ever) it runs, so it's a supplement to the
// foreground refill, not a guarantee.
private func scheduleNotifRefresh() {
    let req = BGAppRefreshTaskRequest(identifier: notifRefreshId)
    req.earliestBeginDate = Date(timeIntervalSinceNow: 6 * 60 * 60) // ~6h
    try? BGTaskScheduler.shared.submit(req)
}

@main
struct iOSApp: App {
    @Environment(\.scenePhase) private var scenePhase

    init() {
        DIKt.startKoinForIos()
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { _, _ in }
        NotificationScheduler.shared.start() // build the reminder window + re-arm on any change
        BGTaskScheduler.shared.register(forTaskWithIdentifier: notifRefreshId, using: nil) { task in
            NotificationScheduler.shared.rebuildAsync()
            scheduleNotifRefresh()               // chain the next one
            task.setTaskCompleted(success: true)
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
        .onChange(of: scenePhase) { phase in
            if phase == .active { NotificationScheduler.shared.rebuildAsync() } // top up on every open
            if phase == .background { scheduleNotifRefresh() }
        }
    }
}
