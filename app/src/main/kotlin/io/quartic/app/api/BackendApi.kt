package io.quartic.app.api

import io.quartic.app.KeyManager
import io.quartic.app.authHttpClient
import io.quartic.app.clientOf
import io.quartic.app.state.ApplicationState
import io.quartic.tracker.api.RegistrationRequest
import io.quartic.tracker.api.RegistrationResponse
import io.quartic.tracker.api.UploadRequest
import okhttp3.OkHttpClient
import retrofit2.http.Body
import retrofit2.http.POST
import rx.Observable

interface BackendApi {
    @POST("users/register")
    fun register(@Body request: RegistrationRequest): Observable<RegistrationResponse>

    @POST("upload")
    fun upload(@Body request: UploadRequest): Observable<Int>

    companion object {
        fun unauthedBackendClient(state: ApplicationState): BackendApi = clientOf(state.configuration.backendBaseUrl, OkHttpClient())

        fun authedBackendClient(state: ApplicationState): BackendApi = clientOf(
                state.configuration.backendBaseUrl,
                authHttpClient(KeyManager(state), { state.userId!! })
        )

    }
}