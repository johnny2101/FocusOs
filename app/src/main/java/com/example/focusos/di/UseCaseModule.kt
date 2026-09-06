package com.example.focusos.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {
    // Concrete UseCase classes with @Inject constructors do not need @Binds methods.
    // Dagger/Hilt automatically provides them.
}