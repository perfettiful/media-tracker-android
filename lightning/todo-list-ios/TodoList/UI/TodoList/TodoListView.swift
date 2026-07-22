import SwiftUI

// main screen. reads state from the view model and sends events back,
// same shape as TodoListScreen(viewModel) on the android side.
// NavigationStack = NavHost, .sheet = dialog destination, .toolbar = app bar slots
struct TodoListView: View {
    // re-renders when @Published values change, like collectAsState()
    @ObservedObject var viewModel: TodoListViewModel

    // local ui state nobody else needs, remember { mutableStateOf(false) }
    @State private var isAddingTodo = false
    @State private var editMode: EditMode = .inactive

    private var isEditing: Bool { editMode.isEditing }

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
                // edit mode brings drag handles for reorder and minus buttons
                // for delete, all free from the platform
                ToolbarItem(placement: .topBarLeading) {
                    Button(isEditing ? "Done" : "Edit") {
                        withAnimation { editMode = isEditing ? .inactive : .active }
                    }
                    .fontWeight(isEditing ? .semibold : .regular)
                }
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
            // the composable("todo/{todoId}") destination
            .navigationDestination(for: UUID.self) { itemId in
                TodoDetailView(itemId: itemId, viewModel: viewModel)
            }
            .sheet(isPresented: $isAddingTodo) {
                // sheet owns its draft, result comes back up through the callback
                AddTodoView { title in
                    viewModel.add(title: title)
                }
            }
        }
    }

    // not a .bottomBar toolbar on purpose: ios 26 bridges that to a legacy
    // UIKit toolbar and spams console warnings. safeAreaInset is the
    // Scaffold bottomBar slot anyway
    private var bottomStatusBar: some View {
        HStack {
            // concatenated Text is buildAnnotatedString, gets the count its own color
            (Text("\(viewModel.activeCount)")
                .fontWeight(.semibold)
                .foregroundStyle(viewModel.activeCount == 0 ? Theme.doneHighlight : Theme.activeHighlight)
            + Text(" remaining")
                .foregroundStyle(.secondary))
                .font(.footnote)
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
                .tint(Theme.doneHighlight)
            (Text("\(viewModel.completedCount)")
                .fontWeight(.semibold)
                .foregroundStyle(Theme.doneHighlight)
            + Text(" of \(viewModel.items.count) done")
                .foregroundStyle(.secondary))
                .font(.caption)
                .fixedSize()
        }
        .padding(.horizontal, Theme.screenPadding)
        .padding(.bottom, Theme.rowSpacing)
        .animation(.snappy, value: viewModel.completionFraction)
    }

    // List + ForEach is LazyColumn + items(key = { it.id })
    private var todoList: some View {
        List {
            ForEach(viewModel.visibleItems) { item in
                Group {
                    if isEditing {
                        // no chevron while editing, the drag handle owns that edge
                        TodoRowView(item: item, isEditing: true) {
                            viewModel.toggle(item)
                        }
                    } else {
                        // typed route, navController.navigate("todo/${item.id}")
                        NavigationLink(value: item.id) {
                            TodoRowView(item: item) {
                                viewModel.toggle(item)
                            }
                        }
                    }
                }
                .swipeActions(edge: .leading, allowsFullSwipe: true) {
                    Button {
                        viewModel.toggle(item)
                    } label: {
                        Label(item.isDone ? "Undo" : "Done",
                              systemImage: item.isDone ? "arrow.uturn.backward" : "checkmark")
                    }
                    .tint(item.isDone ? Theme.activeHighlight : Theme.doneHighlight)
                }
            }
            .onDelete { offsets in
                viewModel.delete(atVisibleOffsets: offsets)
            }
            .onMove { source, destination in
                viewModel.move(fromVisibleOffsets: source, toVisibleOffset: destination)
            }
        }
        .listStyle(.insetGrouped)
        .animation(.default, value: viewModel.visibleItems)
        .environment(\.editMode, $editMode)
    }
}

// shown when the current filter has nothing to display
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

// previews with the fake repo, like @Preview composables
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
