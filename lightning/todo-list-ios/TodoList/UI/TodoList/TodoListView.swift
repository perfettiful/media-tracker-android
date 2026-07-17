import SwiftUI

// second pass: local @State drives the UI, same mental model as
// remember { mutableStateOf(...) } in Compose. $filter hands the Picker
// a two-way Binding; the add sheet hoists its result up via a callback.
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

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                Picker("Filter", selection: $filter) {
                    ForEach(Filter.allCases) { filter in
                        Text(filter.rawValue).tag(filter)
                    }
                }
                .pickerStyle(.segmented)
                .padding()

                List {
                    ForEach(visibleItems) { item in
                        Text(item.title)
                            .strikethrough(item.isDone)
                            .foregroundStyle(item.isDone ? .secondary : .primary)
                            .contentShape(Rectangle())
                            .onTapGesture { toggle(item) }
                    }
                    .onDelete { offsets in
                        delete(atVisibleOffsets: offsets)
                    }
                }
                .listStyle(.insetGrouped)
            }
            .navigationTitle("Todos")
            .toolbar {
                Button("Add") { isAddingTodo = true }
            }
            .sheet(isPresented: $isAddingTodo) {
                AddTodoView { title in
                    items.insert(TodoItem(title: title), at: 0)
                }
            }
        }
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

#Preview {
    TodoListView()
}
