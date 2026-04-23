package com.dreamweddingstories.tv.di

import android.content.Context
import android.content.SharedPreferences
import com.dreamweddingstories.tv.network.VimeoApiService
import com.dreamweddingstories.tv.network.RetrofitClient
import com.dreamweddingstories.tv.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences =
        context.getSharedPreferences(Constants.SESSION_PREFS_NAME, Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideVimeoApiService(): VimeoApiService = RetrofitClient.createVimeoApiService()
}
