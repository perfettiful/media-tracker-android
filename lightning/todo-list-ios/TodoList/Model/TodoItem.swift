import Foundation

// like a Kotlin data class. Identifiable gives List a stable key,
// same job as key = { it.id } in LazyColumn
struct TodoItem: Identifiable {
    let id = UUID()
    var title: String
    var isDone = false
}
