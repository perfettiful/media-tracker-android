import SwiftUI

/// Sheet for creating a todo.
/// Android analogue: an AlertDialog / bottom-sheet destination with a TextField.
/// The draft text is local UI state (`remember { mutableStateOf("") }`), and the
/// finished title is hoisted up through `onAdd` — the screen never reaches in.
struct AddTodoView: View {
    let onAdd: (String) -> Void

    /// @Environment(\.dismiss) ≈ navController.popBackStack() / onDismissRequest.
    @Environment(\.dismiss) private var dismiss
    @State private var title = ""
    @FocusState private var titleFieldFocused: Bool

    private var trimmedTitle: String {
        title.trimmingCharacters(in: .whitespacesAndNewlines)
    }

    var body: some View {
        NavigationStack {
            Form {
                TextField("What needs doing?", text: $title)
                    .focused($titleFieldFocused)
                    .submitLabel(.done)
                    .onSubmit(addAndDismiss)
            }
            .navigationTitle("New Todo")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Add", action: addAndDismiss)
                        .disabled(trimmedTitle.isEmpty)
                }
            }
            .onAppear { titleFieldFocused = true }
        }
        .presentationDetents([.medium])
        .presentationDragIndicator(.visible)
    }

    private func addAndDismiss() {
        guard !trimmedTitle.isEmpty else { return }
        onAdd(trimmedTitle)
        dismiss()
    }
}

#Preview {
    AddTodoView { _ in }
}
