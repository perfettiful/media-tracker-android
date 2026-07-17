import SwiftUI

/// One row in the list.
/// Android analogue: a stateless `TodoRow(item, onToggle)` composable —
/// it receives data and reports events, but owns no state of its own.
struct TodoRowView: View {
    let item: TodoItem
    let onToggle: () -> Void

    var body: some View {
        HStack(spacing: Theme.rowSpacing) {
            Button(action: onToggle) {
                Image(systemName: item.isDone ? "checkmark.circle.fill" : "circle")
                    .font(.title2)
                    .foregroundStyle(item.isDone ? Theme.doneHighlight : .secondary)
                    .contentTransition(.symbolEffect(.replace))
            }
            .buttonStyle(.plain)
            .accessibilityLabel(item.isDone ? "Mark as not done" : "Mark as done")

            VStack(alignment: .leading, spacing: 2) {
                Text(item.title)
                    .strikethrough(item.isDone, color: Theme.doneHighlight.opacity(0.8))
                    .foregroundStyle(item.isDone ? .secondary : .primary)

                Text(item.createdAt, format: .relative(presentation: .named))
                    .font(.caption)
                    .foregroundStyle(.tertiary)
            }
        }
        .padding(.vertical, 4)
    }
}

#Preview(traits: .sizeThatFitsLayout) {
    List {
        TodoRowView(item: TodoItem(title: "An active todo")) {}
        TodoRowView(item: TodoItem(title: "A finished todo", isDone: true)) {}
    }
}
