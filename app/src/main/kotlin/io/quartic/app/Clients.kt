package io.quartic.app

import android.util.Base64
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.quartic.app.crypto.KeyManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory

inline fun <reified T : Any> clientOf(baseUrl: String, client: OkHttpClient): T = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addConverterFactory(JacksonConverterFactory.create(ObjectMapper().registerKotlinModule()))
        .build().create(T::class.java)

fun authHttpClient(keyManager: KeyManager, userId: () -> String): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
            val buffer = okio.Buffer()
            request.body().writeTo(buffer)
            val data = buffer.readByteArray()
            val requestBuilder = request.newBuilder()
            val signature = keyManager.sign(data)
            val base64Signature = Base64.encodeToString(signature, Base64.NO_WRAP)
            requestBuilder.addHeader("Authorization", "QuarticAuth userId=\"${userId()}\", signature=\"$base64Signature\"")
            chain.proceed(requestBuilder.build())
        }
        .build()


