package com.yash.Speachr.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import android.util.Log
import java.net.Proxy

val networkModule = module {
    single {
        HttpClient(OkHttp) {
            install(HttpTimeout) {
                requestTimeoutMillis = 100_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }
            engine {
                config {
                    proxy(Proxy.NO_PROXY)
                    retryOnConnectionFailure(true)
                }
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("HTTP", message)
                    }
                }
                level = LogLevel.ALL
            }
            defaultRequest {
                url("http://localhost:3000/")
            }
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }
}
