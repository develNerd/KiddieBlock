package data.models

import kotlinx.serialization.Serializable

@Serializable
data class Quiz(
    val answers: List<String>,
    val correctAnswer: Int,
    val question: String
)