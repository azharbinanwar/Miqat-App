import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        DIKt.startKoinForIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
