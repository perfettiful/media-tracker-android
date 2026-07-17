import SwiftUI

// add sheet. owns its own draft text and hands the final title up through
// onAdd, the list screen never reaches in. dialog destination, basically
struct AddTodoView: View {
    let onAdd: (String) -> Void

    // dismiss() is popBackStack / onDismissRequest
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
