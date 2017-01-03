package io.quartic.app.api

import io.quartic.tracker.api.RegistrationRequest
import io.quartic.tracker.api.RegistrationResponse
import retrofit2.http.Body
import retrofit2.http.POST
import rx.Observable

interface RegistrationService {
    @POST("register")
    fun register(@Body request: RegistrationRequest): Observable<RegistrationResponse>
}