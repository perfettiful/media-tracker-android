import SwiftUI

// entry point. WindowGroup is basically MainActivity + setContent { },
// and wiring the repo/viewmodel here by hand is what Hilt would do for us
@main
struct TodoListApp: App {
    // @StateObject survives re-renders, same job as viewModel() in Compose
    @StateObject private var viewModel = TodoListViewModel(
        repository: UserDefaultsTodoRepository()
    )

    var body: some Scene {
        WindowGroup {
            TodoListView(viewModel: viewModel)
                // dark only, like always handing MaterialTheme darkColorScheme()
                .preferredColorScheme(.dark)
        }
    }
}
