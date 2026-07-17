import Foundation

/// Screen-level state holder for the todo list.
/// Android analogue: a Jetpack `ViewModel`.
/// - `ObservableObject` + `@Published` ≈ exposing `StateFlow`s that the UI collects
/// - `@MainActor` ≈ confining state mutation to the main dispatcher
/// - constructor injection of the repository ≈ Hilt @Inject constructor
/// (iOS 17's newer `@Observable` macro is closer to Compose snapshot state;
/// ObservableObject is used here because it maps most directly onto StateFlow.)
@MainActor
final class TodoListViewModel: ObservableObject {

    enum Filter: String, CaseIterable, Identifiable {
        case all = "All"
        case active = "Active"
        case done = "Done"

        var id: String { rawValue }
    }

    /// UI state. `private(set)` keeps writes inside the view model —
    /// the same reason Android exposes StateFlow but keeps MutableStateFlow private.
    @Published private(set) var items: [TodoItem] = []
    @Published var filter: Filter = .all

    private let repository: TodoRepository

    init(repository: TodoRepository) {
        self.repository = repository
        items = repository.load()
    }

    // MARK: - Derived state (like a `combine`d/`map`ped StateFlow)

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

    /// 0…1 fraction of items completed, drives the header progress bar.
    var completionFraction: Double {
        items.isEmpty ? 0 : Double(completedCount) / Double(items.count)
    }

    var hasCompletedItems: Bool {
        items.contains { $0.isDone }
    }

    // MARK: - Events (UI calls down, state flows back up — unidirectional data flow)

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

    /// Offsets come from the *filtered* list the user sees, so map them
    /// back to ids before removing from the source of truth.
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
