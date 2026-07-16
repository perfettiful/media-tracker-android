import SwiftUI

// entry point, WindowGroup plays the role of MainActivity + setContent { }
@main
struct TodoListApp: App {
    var body: some Scene {
        WindowGroup {
            TodoListView()
        }
    }
}
