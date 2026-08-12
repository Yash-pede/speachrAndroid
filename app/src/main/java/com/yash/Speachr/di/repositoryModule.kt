package com.yash.Speachr.di

import com.yash.Speachr.repository.AudioRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { AudioRepository(get()) }
}
