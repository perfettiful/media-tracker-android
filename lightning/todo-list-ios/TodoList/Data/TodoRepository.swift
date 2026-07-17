import Foundation

/// Abstraction over where todos live.
/// Android analogue: the repository interface in `data/` (see MediaRepository.kt
/// in media-tracker-android). Swift `protocol` ≈ Kotlin `interface`.
protocol TodoRepository {
    func load() -> [TodoItem]
    func save(_ items: [TodoItem])
}

/// Persists todos as JSON in UserDefaults.
/// Android analogue: DataStore/SharedPreferences-backed repository. For anything
/// bigger you'd reach for SwiftData or Core Data, the way Android reaches for Room.
struct UserDefaultsTodoRepository: TodoRepository {
    private let key = "todo.items"
    private let defaults: UserDefaults

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }

    func load() -> [TodoItem] {
        guard let data = defaults.data(forKey: key),
              let items = try? JSONDecoder().decode([TodoItem].self, from: data) else {
            return []
        }
        return items
    }

    func save(_ items: [TodoItem]) {
        guard let data = try? JSONEncoder().encode(items) else { return }
        defaults.set(data, forKey: key)
    }
}

/// Non-persisting implementation for previews and tests.
/// Android analogue: FakeMediaRepository.kt in media-tracker-android.
final class InMemoryTodoRepository: TodoRepository {
    private var items: [TodoItem]

    init(items: [TodoItem] = []) {
        self.items = items
    }

    func load() -> [TodoItem] { items }
    func save(_ items: [TodoItem]) { self.items = items }
}
