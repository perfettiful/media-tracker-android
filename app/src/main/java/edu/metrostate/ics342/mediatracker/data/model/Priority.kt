package edu.metrostate.ics342.mediatracker.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Priority(
    val mediaId: Int,
    // 1 = high, 3 = low. separate from orderIndex, two items can share a level
    val priority: Int,
    val orderIndex: Int,
    val estimatedTimeHours: Int? = null,
    val notes: String? = null,
    val media: Media? = null,
)

// PUT /priorities replaces the whole row, it doesnt merge, so always send
// every field you want to keep or the api nulls it out
@Serializable
data class UpdatePriorityRequest(
    val mediaId: Int,
    val priority: Int,
    val orderIndex: Int,
    val estimatedTimeHours: Int? = null,
    val notes: String? = null,
)

enum class PriorityLevel(val apiValue: Int) {
    HIGH(1),
    MEDIUM(2),
    LOW(3);

    companion object {
        fun from(value: Int): PriorityLevel = entries.find { it.apiValue == value } ?: MEDIUM
    }
}

const val MAX_PRIORITIES = 5
