package data.models

import kotlinx.serialization.Serializable

@Serializable
data class Lesson(
    val images: List<String>,
    val name: String,
    val quize: Quiz
)