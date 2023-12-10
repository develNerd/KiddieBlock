package data

import data.models.Data
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json

class ApiClient {
    val client = HttpClient(CIO) {
        install(ContentNegotiation){
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
        install(Logging){
            logger = Logger.DEFAULT
            level = LogLevel.ALL
            logger = object: Logger {
                override fun log(message: String) {
                    //println("HTTP Client $message")
                }
            }
        }
    }

    suspend fun getLessons():Data{
        return get<Data>("https://us-central1-traceit-ae280.cloudfunctions.net/app/test")
    }

    suspend inline fun <reified T> get(url: String):T{
        return client.get {
            contentType(ContentType.Application.Json)
            url(url)
        }.body()
    }
}