package io.quartic.app.api

import android.util.Log
import io.quartic.app.KeyManager
import io.quartic.app.authHttpClient
import io.quartic.app.clientOf
import io.quartic.app.state.ApplicationState
import okhttp3.OkHttpClient

fun unauthedBackendClient(state: ApplicationState): BackendApi = clientOf(state.configuration.backendBaseUrl, OkHttpClient())

fun authedBackendClient(state: ApplicationState): BackendApi {
    Log.i("Weird", "${state}")
    Log.i("Weird", "${state.configuration}")
    Log.i("Weird", "${state.configuration.backendBaseUrl}")
    return clientOf(
            state.configuration.backendBaseUrl,
            authHttpClient(KeyManager(state), state.userId!!)
    )
}
