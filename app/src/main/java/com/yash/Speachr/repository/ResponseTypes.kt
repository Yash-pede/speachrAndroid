package com.yash.Speachr.repository

import kotlinx.serialization.Serializable

@Serializable
data class AudioTranscribeApiResponse(
    val text: String,
    val sourceLanguage: String,
)
