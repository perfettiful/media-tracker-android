import SwiftUI

/// App entry point.
/// Android analogue: `MainActivity` + `Application` class. `WindowGroup` plays the
/// role of `setContent { }`, and building the repository/view model here is the
/// "composition root" — the hand-rolled version of what Hilt does with @Inject.
@main
struct TodoListApp: App {
    /// @StateObject = "create once and keep alive across re-renders",
    /// the same job `viewModel()` / `hiltViewModel()` does in Compose.
    @StateObject private var viewModel = TodoListViewModel(
        repository: UserDefaultsTodoRepository()
    )

    var body: some Scene {
        WindowGroup {
            TodoListView(viewModel: viewModel)
                // dark-only app: the SwiftUI version of always passing
                // darkColorScheme() to MaterialTheme instead of isSystemInDarkTheme()
                .preferredColorScheme(.dark)
        }
    }
}
