import SwiftUI

// first pass: List + ForEach over a hardcoded array,
// the SwiftUI spelling of LazyColumn + items()
struct TodoListView: View {
    private let items = [
        TodoItem(title: "Compare SwiftUI to Compose"),
        TodoItem(title: "Build the todo list UI"),
        TodoItem(title: "Write the README", isDone: true),
    ]

    var body: some View {
        NavigationStack {
            List(items) { item in
                Text(item.title)
                    .strikethrough(item.isDone)
                    .foregroundStyle(item.isDone ? .secondary : .primary)
            }
            .navigationTitle("Todos")
        }
    }
}

#Preview {
    TodoListView()
}
