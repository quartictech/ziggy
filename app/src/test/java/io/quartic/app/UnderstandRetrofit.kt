package io.quartic.app

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.Options.DYNAMIC_PORT
import com.github.tomakehurst.wiremock.junit.WireMockRule
import io.quartic.app.api.BackendApi
import io.quartic.tracker.api.RegistrationRequest
import org.apache.http.HttpHeaders.CONTENT_TYPE
import org.apache.http.entity.ContentType.APPLICATION_JSON
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory

class UnderstandRetrofit {
    @get:Rule
    val wireMockRule = WireMockRule(DYNAMIC_PORT)

    @Test
    fun name() {
        stubFor(WireMock.post(urlEqualTo("/register"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON.mimeType)
                        .withBody("{ \"userId\": \"42\" }")
                )
        )

        val request = RegistrationRequest("abc", "def")

        val retrofit = Retrofit.Builder()
                .baseUrl("http://localhost:${wireMockRule.port()}")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create(ObjectMapper().registerKotlinModule()))
                .build()

        val registration = retrofit.create(BackendApi::class.java)

        val observable = registration.register(request)

        observable.subscribe(
                { println("Success: $it") },
                { println("Error: $it") }
        )
    }
}
