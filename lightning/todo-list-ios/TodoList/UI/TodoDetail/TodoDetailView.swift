import SwiftUI

// detail screen. gets an id from navigation and looks the item up in shared
// state, same as mediaId flowing into MediaDetailScreen. dismiss() is popBackStack()
struct TodoDetailView: View {
    let itemId: UUID
    @ObservedObject var viewModel: TodoListViewModel

    @Environment(\.dismiss) private var dismiss
    @State private var draftTitle = ""

    private var item: TodoItem? {
        viewModel.item(withId: itemId)
    }

    var body: some View {
        Group {
            if let item {
                detailForm(for: item)
            } else {
                // item got deleted while this screen was on the stack
                ContentUnavailableView("Todo Deleted", systemImage: "trash")
            }
        }
        .navigationTitle("Details")
        .navigationBarTitleDisplayMode(.inline)
    }

    private func detailForm(for item: TodoItem) -> some View {
        Form {
            Section("Title") {
                TextField("Title", text: $draftTitle)
                    .onAppear { draftTitle = item.title }
                    .onSubmit { viewModel.rename(item, to: draftTitle) }

                // only show save when the draft actually changed
                if draftTitle != item.title,
                   !draftTitle.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                    Button("Save Title") {
                        viewModel.rename(item, to: draftTitle)
                    }
                    .foregroundStyle(Color.accentColor)
                }
            }

            Section("Status") {
                LabeledContent("Status") {
                    Label(item.isDone ? "Done" : "In Progress",
                          systemImage: item.isDone ? "checkmark.circle.fill" : "circle.dashed")
                        .font(.subheadline.weight(.medium))
                        .foregroundStyle(item.isDone ? Theme.doneHighlight : Theme.activeHighlight)
                }

                LabeledContent("Created") {
                    Text(item.createdAt, format: .dateTime.month(.abbreviated).day().year().hour().minute())
                }

                Button {
                    viewModel.toggle(item)
                } label: {
                    Label(item.isDone ? "Mark as Not Done" : "Mark as Done",
                          systemImage: item.isDone ? "arrow.uturn.backward.circle" : "checkmark.circle")
                }
                .foregroundStyle(item.isDone ? Theme.activeHighlight : Theme.doneHighlight)
            }

            Section {
                Button("Delete Todo", role: .destructive) {
                    viewModel.delete(item)
                    dismiss()
                }
            }
        }
    }
}

#Preview {
    let repository = InMemoryTodoRepository(items: [
        TodoItem(title: "Practice the lightning demo"),
    ])
    let viewModel = TodoListViewModel(repository: repository)
    return NavigationStack {
        TodoDetailView(itemId: viewModel.items[0].id, viewModel: viewModel)
    }
    .preferredColorScheme(.dark)
}
