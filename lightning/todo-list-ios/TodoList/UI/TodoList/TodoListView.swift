import SwiftUI

// third pass: SF Symbols via Image(systemName:) so no icon assets needed,
// plus the native list niceties: swipe actions, a progress header,
// and a real empty state.
struct TodoListView: View {
    enum Filter: String, CaseIterable, Identifiable {
        case all = "All"
        case active = "Active"
        case done = "Done"

        var id: String { rawValue }
    }

    @State private var items = [
        TodoItem(title: "Compare SwiftUI to Compose"),
        TodoItem(title: "Build the todo list UI"),
        TodoItem(title: "Write the README", isDone: true),
    ]
    @State private var filter: Filter = .all
    @State private var isAddingTodo = false

    private var visibleItems: [TodoItem] {
        switch filter {
        case .all: return items
        case .active: return items.filter { !$0.isDone }
        case .done: return items.filter { $0.isDone }
        }
    }

    private var activeCount: Int { items.filter { !$0.isDone }.count }
    private var completedCount: Int { items.count - activeCount }
    private var hasCompletedItems: Bool { items.contains { $0.isDone } }
    private var completionFraction: Double {
        items.isEmpty ? 0 : Double(completedCount) / Double(items.count)
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                filterPicker

                if !items.isEmpty {
                    progressHeader
                }

                if visibleItems.isEmpty {
                    EmptyStateView(filter: filter)
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
                AddTodoView { title in
                    items.insert(TodoItem(title: title), at: 0)
                }
            }
        }
    }

    // pure SwiftUI bottom bar instead of a .bottomBar toolbar: on iOS 26
    // the toolbar version bridges to a legacy UIKit toolbar and spams
    // "Adding 'UIKitToolbar' as a subview..." warnings in the console
    private var bottomStatusBar: some View {
        HStack {
            Text("\(activeCount) remaining")
                .font(.footnote)
                .foregroundStyle(.secondary)
            Spacer()
            Button("Clear Completed", role: .destructive) {
                items.removeAll { $0.isDone }
            }
            .font(.footnote)
            .disabled(!hasCompletedItems)
        }
        .padding(.horizontal, Theme.screenPadding)
        .padding(.vertical, 12)
        .background(.bar)
    }

    private var filterPicker: some View {
        Picker("Filter", selection: $filter) {
            ForEach(Filter.allCases) { filter in
                Text(filter.rawValue).tag(filter)
            }
        }
        .pickerStyle(.segmented)
        .padding(.horizontal, Theme.screenPadding)
        .padding(.vertical, Theme.rowSpacing)
    }

    private var progressHeader: some View {
        HStack(spacing: Theme.rowSpacing) {
            ProgressView(value: completionFraction)
                .tint(.accentColor)
            Text("\(completedCount) of \(items.count) done")
                .font(.caption)
                .foregroundStyle(.secondary)
                .fixedSize()
        }
        .padding(.horizontal, Theme.screenPadding)
        .padding(.bottom, Theme.rowSpacing)
        .animation(.snappy, value: completionFraction)
    }

    private var todoList: some View {
        List {
            ForEach(visibleItems) { item in
                TodoRowView(item: item) {
                    toggle(item)
                }
                .swipeActions(edge: .leading, allowsFullSwipe: true) {
                    Button {
                        toggle(item)
                    } label: {
                        Label(item.isDone ? "Undo" : "Done",
                              systemImage: item.isDone ? "arrow.uturn.backward" : "checkmark")
                    }
                    .tint(item.isDone ? .orange : .green)
                }
            }
            .onDelete { offsets in
                delete(atVisibleOffsets: offsets)
            }
        }
        .listStyle(.insetGrouped)
        .animation(.default, value: visibleItems)
    }

    private func toggle(_ item: TodoItem) {
        guard let index = items.firstIndex(where: { $0.id == item.id }) else { return }
        items[index].isDone.toggle()
    }

    // offsets come from the filtered list the user sees, so map them
    // back to ids before removing from the real array
    private func delete(atVisibleOffsets offsets: IndexSet) {
        let ids = offsets.map { visibleItems[$0].id }
        items.removeAll { ids.contains($0.id) }
    }
}

// shown when the current filter has nothing to display
private struct EmptyStateView: View {
    let filter: TodoListView.Filter

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

#Preview {
    TodoListView()
}
