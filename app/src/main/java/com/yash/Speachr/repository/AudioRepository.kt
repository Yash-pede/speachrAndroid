package com.yash.Speachr.repository

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import java.io.File

class AudioRepository(private val client: HttpClient) {

    suspend fun transcribeAudio(file: File): AudioTranscribeApiResponse? {
        Log.d(
            "AudioRepository",
            "Attempting to upload file: ${file.absolutePath} size: ${file.length()}"
        )
        return try {
            val response = client.post("/audio/transcribe") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("file", file.readBytes(), Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                            })
                        }
                    )
                )
            }
            Log.d("AudioRepository", "Response status: ${response.status}")
            val body = response.body<AudioTranscribeApiResponse>()
            Log.d("AudioRepository", "Response body: $body")
            body
        } catch (e: Exception) {
            Log.e("AudioRepository", "UPLOAD FAILED", e)
            null
        }
    }
}
