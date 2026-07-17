import Foundation

// swift protocol = kotlin interface, same repository idea as the android app
protocol TodoRepository {
    func load() -> [TodoItem]
    func save(_ items: [TodoItem])
}

// json in UserDefaults, close enough to DataStore at this size.
// a bigger app would use SwiftData the way android reaches for Room
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

// fake repo for previews, same trick as FakeMediaRepository
final class InMemoryTodoRepository: TodoRepository {
    private var items: [TodoItem]

    init(items: [TodoItem] = []) {
        self.items = items
    }

    func load() -> [TodoItem] { items }
    func save(_ items: [TodoItem]) { self.items = items }
}
