import Foundation

/// Plain value type for a single todo.
/// Android analogue: a Kotlin `data class TodoItem(...)`.
/// - `Identifiable` gives lists a stable key (like `key = { it.id }` in LazyColumn)
/// - `Codable` is the built-in serialization (like @Serializable / Moshi)
/// - `Equatable` comes free on data classes in Kotlin; here we declare it
struct TodoItem: Identifiable, Codable, Equatable {
    let id: UUID
    var title: String
    var isDone: Bool
    let createdAt: Date

    init(id: UUID = UUID(), title: String, isDone: Bool = false, createdAt: Date = .now) {
        self.id = id
        self.title = title
        self.isDone = isDone
        self.createdAt = createdAt
    }
}
