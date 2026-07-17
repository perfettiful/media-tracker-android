import Foundation

// the ViewModel. ObservableObject + @Published is the StateFlow setup,
// @MainActor keeps mutations on the main thread like viewModelScope.
// (ios 17 has a newer @Observable macro, closer to Compose snapshot state,
// but this maps onto ViewModel + StateFlow more directly)
@MainActor
final class TodoListViewModel: ObservableObject {

    enum Filter: String, CaseIterable, Identifiable {
        case all = "All"
        case active = "Active"
        case done = "Done"

        var id: String { rawValue }
    }

    // private(set) for the same reason the android app keeps
    // MutableStateFlow private and only exposes StateFlow
    @Published private(set) var items: [TodoItem] = []
    @Published var filter: Filter = .all

    private let repository: TodoRepository

    init(repository: TodoRepository) {
        self.repository = repository
        items = repository.load()
    }

    var visibleItems: [TodoItem] {
        switch filter {
        case .all: return items
        case .active: return items.filter { !$0.isDone }
        case .done: return items.filter { $0.isDone }
        }
    }

    var activeCount: Int {
        items.filter { !$0.isDone }.count
    }

    var completedCount: Int {
        items.count - activeCount
    }

    var completionFraction: Double {
        items.isEmpty ? 0 : Double(completedCount) / Double(items.count)
    }

    var hasCompletedItems: Bool {
        items.contains { $0.isDone }
    }

    // the detail route carries an id, not the whole object
    func item(withId id: UUID) -> TodoItem? {
        items.first { $0.id == id }
    }

    func add(title: String) {
        let trimmed = title.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        items.insert(TodoItem(title: trimmed), at: 0)
        persist()
    }

    func toggle(_ item: TodoItem) {
        guard let index = items.firstIndex(where: { $0.id == item.id }) else { return }
        items[index].isDone.toggle()
        persist()
    }

    func rename(_ item: TodoItem, to title: String) {
        let trimmed = title.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty,
              let index = items.firstIndex(where: { $0.id == item.id }) else { return }
        items[index].title = trimmed
        persist()
    }

    func delete(_ item: TodoItem) {
        items.removeAll { $0.id == item.id }
        persist()
    }

    // offsets are into the filtered list the user sees, map to ids first
    func delete(atVisibleOffsets offsets: IndexSet) {
        let ids = offsets.map { visibleItems[$0].id }
        items.removeAll { ids.contains($0.id) }
        persist()
    }

    func clearCompleted() {
        items.removeAll { $0.isDone }
        persist()
    }

    private func persist() {
        repository.save(items)
    }
}
