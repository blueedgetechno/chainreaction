import UIKit
import shared
import FirebaseCore

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        FirebaseApp.configure()

        let firebaseRepo = SwiftFirebaseRepo()
        window = UIWindow(frame: UIScreen.main.bounds)
        let rootViewController = MainViewControllerKt.MainViewController(firebaseBridge: firebaseRepo)
        window?.rootViewController = rootViewController
        window?.makeKeyAndVisible()
        return true
    }
}
