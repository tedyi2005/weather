package com.android.weather.module

import android.content.Context
import com.android.weather.apis.WeatherApi
import com.google.gson.GsonBuilder
import com.android.weather.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 *  Retrofit Networking configuration module
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @RetrofitClient
    @Singleton
    fun getRetrofitClient(@ApplicationContext appContext: Context): Retrofit {
        val builder = GsonBuilder().setLenient().create()
        val baseUrl = BuildConfig.BASE_URL
        // Retrofit builder with basic configuration
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(builder))
            .baseUrl(baseUrl)
            .client(buildHttpClient(appContext))
            .build()
    }

    private fun buildHttpClient(context: Context, timeout: Long = 30): OkHttpClient {
        return getHttpClientBuilder(context, timeout).build()
    }

    // Http client builder with 30 sec timeout
    private fun getHttpClientBuilder(context: Context, timeout: Long = 30): OkHttpClient.Builder {
        val builder = OkHttpClient.Builder()
        // set cache
        builder.cache(provideCache(context))
        if (BuildConfig.DEBUG) {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(httpLoggingInterceptor)
        }
        builder.addInterceptor(provideCacheInterceptor())
        return builder
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
    }

    // Cache Interceptor to avoid re-downloading
    private fun provideCacheInterceptor(): Interceptor {
        return Interceptor { chain ->
            var request = chain.request()
            val originalResponse = chain.proceed(request)
            val cacheControl = originalResponse.header("Cache-Control")
            if (cacheControl == null || cacheControl.contains("no-store") || cacheControl.contains("no-cache") ||
                cacheControl.contains("must-revalidate") || cacheControl.contains("max-stale=0")
            ) {
                originalResponse.close()
                val cacheControlSetting = CacheControl.Builder().maxStale(1, TimeUnit.DAYS).build()
                request = request.newBuilder().cacheControl(cacheControlSetting).build()
                chain.proceed(request)
            } else {
                originalResponse
            }
        }
    }

    private fun provideCache(context: Context): Cache {
        val cacheSize = 10 * 1024 * 1024 // this is 10MB
        return Cache(File(context.cacheDir, "http-cache"), cacheSize.toLong())
    }

    // API related dependencies
    @Provides
    fun getSearchApi(@RetrofitClient retrofit: Retrofit): WeatherApi =
        retrofit.create(WeatherApi::class.java)

}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RetrofitClient
