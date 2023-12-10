package data.models

import kotlinx.serialization.Serializable

@Serializable
data class Data(
    val data: List<Lesson> = emptyList()

)