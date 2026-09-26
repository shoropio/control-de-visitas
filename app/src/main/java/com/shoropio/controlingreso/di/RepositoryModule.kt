package com.shoropio.controlingreso.di

import com.shoropio.controlingreso.data.preferences.AppPreferences
import com.shoropio.controlingreso.data.preferences.SettingsRepository
import com.shoropio.controlingreso.data.repository.AccessRecordRepository
import com.shoropio.controlingreso.domain.repository.AccessRepository
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
    abstract fun bindAccessRepository(impl: AccessRecordRepository): AccessRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: AppPreferences): SettingsRepository
}