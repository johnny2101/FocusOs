package com.example.focusos.di

import com.example.focusos.data.repository.AppLauncherRepositoryImpl
import com.example.focusos.domain.repository.AppLauncherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
}