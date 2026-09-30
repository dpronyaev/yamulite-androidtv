package dev.pdv.yamulite.tv.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.pdv.yamulite.tv.BuildConfig
import dev.pdv.yamulite.tv.data.music.AuthInterceptor
import dev.pdv.yamulite.tv.data.music.MusicApi
import dev.pdv.yamulite.tv.data.music.TokenRefreshAuthenticator
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MusicRetrofit

@Module
@InstallIn(SingletonComponent::class)
object MusicModule {

    @Provides @Singleton
    @MusicRetrofit
    fun provideMusicRetrofit(
        json: Json,
        authInterceptor: AuthInterceptor,
        tokenRefreshAuthenticator: TokenRefreshAuthenticator,
        @ApplicationContext context: Context,
        connectionPool: ConnectionPool,
        dispatcher: Dispatcher,
    ): Retrofit {
        val client = OkHttpClient.Builder()
            .cache(Cache(File(context.cacheDir, "http_music_cache"), 20L * 1024 * 1024))
            .connectionPool(connectionPool)
            .dispatcher(dispatcher)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .authenticator(tokenRefreshAuthenticator)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
                }
            }
            .build()
        return Retrofit.Builder()
            .baseUrl("https://api.music.yandex.net/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides @Singleton
    fun provideMusicApi(@MusicRetrofit retrofit: Retrofit): MusicApi =
        retrofit.create(MusicApi::class.java)
}
