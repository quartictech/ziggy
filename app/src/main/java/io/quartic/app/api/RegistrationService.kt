package io.quartic.app.api

import retrofit2.http.POST

interface RegistrationService {
    @POST("/register")
    fun register(request: RegistrationRequest)
}