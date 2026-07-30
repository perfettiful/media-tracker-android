import Foundation

// like a Kotlin data class. Identifiable gives List a stable key,
// Codable handles the json (think @Serializable)
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
