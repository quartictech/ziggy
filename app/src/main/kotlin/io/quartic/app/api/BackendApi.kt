package io.quartic.app.api

import io.quartic.tracker.api.RegistrationRequest
import io.quartic.tracker.api.RegistrationResponse
import io.quartic.tracker.api.UploadRequest
import retrofit2.http.Body
import retrofit2.http.POST
import rx.Observable

interface BackendApi {
    @POST("users/register")
    fun register(@Body request: RegistrationRequest): Observable<RegistrationResponse>

    @POST("upload")
    fun upload(@Body request: UploadRequest): Observable<Void>
}