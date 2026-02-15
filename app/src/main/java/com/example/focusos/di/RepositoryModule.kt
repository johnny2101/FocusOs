package com.example.focusos.di

import com.example.focusos.data.repository.AppLauncherRepositoryImpl
import com.example.focusos.data.repository.ConfigRepositoryImpl
import com.example.focusos.domain.repository.AppLauncherRepository
import com.example.focusos.domain.repository.ConfigRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAppLauncherRepository(
        impl: AppLauncherRepositoryImpl
    ): AppLauncherRepository

    @Binds
    @Singleton
    abstract fun bindConfigRepository(
        impl: ConfigRepositoryImpl
    ): ConfigRepository

}