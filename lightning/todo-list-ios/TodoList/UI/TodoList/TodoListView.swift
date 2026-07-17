import SwiftUI

/// The main screen.
/// Android analogue: `TodoListScreen(viewModel) { ... }` — a composable that
/// collects state from its ViewModel and sends events back to it.
/// `NavigationStack` ≈ NavHost, `.sheet` ≈ a dialog/bottom-sheet destination,
/// `.toolbar` ≈ TopAppBar/BottomAppBar slots in Scaffold.
struct TodoListView: View {
    /// @ObservedObject = "re-render when this object's @Published values change",
    /// like `collectAsStateWithLifecycle()` on every flow the screen reads.
    @ObservedObject var viewModel: TodoListViewModel

    /// Local UI state that no one else needs, kept in the view —
    /// like `remember { mutableStateOf(false) }` inside a composable.
    @State private var isAddingTodo = false

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                filterPicker

                if !viewModel.items.isEmpty {
                    progressHeader
                }

                if viewModel.visibleItems.isEmpty {
                    EmptyStateView(filter: viewModel.filter)
                } else {
                    todoList
                }
            }
            .navigationTitle("Todos")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button {
                        isAddingTodo = true
                    } label: {
                        Label("Add Todo", systemImage: "plus")
                    }
                }

            }
            .safeAreaInset(edge: .bottom) {
                bottomStatusBar
            }
            .sheet(isPresented: $isAddingTodo) {
                // State hoisting: the sheet owns its text field, and hands the
                // result up through a callback — events up, state down.
                AddTodoView { title in
                    viewModel.add(title: title)
                }
            }
        }
    }

    /// Bottom bar built in pure SwiftUI rather than a `.bottomBar` toolbar:
    /// on iOS 26 the toolbar version is bridged to a legacy UIKit toolbar and
    /// logs "Adding 'UIKitToolbar' as a subview..." warnings in the console.
    /// `safeAreaInset` ≈ Scaffold's `bottomBar` slot in Compose.
    private var bottomStatusBar: some View {
        HStack {
            Text("\(viewModel.activeCount) remaining")
                .font(.footnote)
                .foregroundStyle(.secondary)
            Spacer()
            Button("Clear Completed", role: .destructive) {
                viewModel.clearCompleted()
            }
            .font(.footnote)
            .disabled(!viewModel.hasCompletedItems)
        }
        .padding(.horizontal, Theme.screenPadding)
        .padding(.vertical, 12)
        .background(.bar)
    }

    private var filterPicker: some View {
        Picker("Filter", selection: $viewModel.filter) {
            ForEach(TodoListViewModel.Filter.allCases) { filter in
                Text(filter.rawValue).tag(filter)
            }
        }
        .pickerStyle(.segmented)
        .padding(.horizontal, Theme.screenPadding)
        .padding(.vertical, Theme.rowSpacing)
    }

    private var progressHeader: some View {
        HStack(spacing: Theme.rowSpacing) {
            ProgressView(value: viewModel.completionFraction)
                .tint(.accentColor)
            Text("\(viewModel.completedCount) of \(viewModel.items.count) done")
                .font(.caption)
                .foregroundStyle(.secondary)
                .fixedSize()
        }
        .padding(.horizontal, Theme.screenPadding)
        .padding(.bottom, Theme.rowSpacing)
        .animation(.snappy, value: viewModel.completionFraction)
    }

    /// `List` + `ForEach` ≈ `LazyColumn` + `items(items, key = { it.id })`.
    private var todoList: some View {
        List {
            ForEach(viewModel.visibleItems) { item in
                TodoRowView(item: item) {
                    viewModel.toggle(item)
                }
                .swipeActions(edge: .leading, allowsFullSwipe: true) {
                    Button {
                        viewModel.toggle(item)
                    } label: {
                        Label(item.isDone ? "Undo" : "Done",
                              systemImage: item.isDone ? "arrow.uturn.backward" : "checkmark")
                    }
                    .tint(item.isDone ? .orange : .green)
                }
            }
            .onDelete { offsets in
                viewModel.delete(atVisibleOffsets: offsets)
            }
        }
        .listStyle(.insetGrouped)
        .animation(.default, value: viewModel.visibleItems)
    }
}

/// Shown when the current filter has nothing to display.
/// Android analogue: a small private composable in the same file.
private struct EmptyStateView: View {
    let filter: TodoListViewModel.Filter

    var body: some View {
        ContentUnavailableView(
            label: {
                Label {
                    Text(title)
                } icon: {
                    Image(systemName: "checklist")
                        .symbolRenderingMode(.hierarchical)
                        .foregroundStyle(Color.accentColor)
                }
            },
            description: {
                Text(message)
            }
        )
        .frame(maxHeight: .infinity)
    }

    private var title: String {
        switch filter {
        case .all: return "No Todos Yet"
        case .active: return "Nothing Left To Do"
        case .done: return "Nothing Done Yet"
        }
    }

    private var message: String {
        switch filter {
        case .all: return "Tap + to add your first todo."
        case .active: return "Everything is checked off. Nice."
        case .done: return "Complete a todo and it will show up here."
        }
    }
}

/// Xcode previews ≈ @Preview composables. The in-memory repository plays the
/// role of the fake repository you'd pass to a preview on Android.
#Preview("With items") {
    TodoListView(viewModel: TodoListViewModel(
        repository: InMemoryTodoRepository(items: [
            TodoItem(title: "Compare SwiftUI to Compose"),
            TodoItem(title: "Write week 8 reflection", isDone: true),
            TodoItem(title: "Push to GitHub"),
        ])
    ))
}

#Preview("Empty") {
    TodoListView(viewModel: TodoListViewModel(repository: InMemoryTodoRepository()))
}
