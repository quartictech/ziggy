package io.quartic.app.state

import android.content.Context
import io.quartic.app.api.BackendApi
import io.quartic.app.authClientOf
import io.quartic.app.clientOf

class ApplicationState(val context: Context, val configuration: ApplicationConfiguration) {
    private val sharedPreferences = context.getSharedPreferences("tracker", 0)

    var userId: String
        get() = sharedPreferences.getString("userId", null)
        set(userId) = sharedPreferences.edit()
                .putString("userId", userId)
                .apply()

    val client: BackendApi
        get() = clientOf(configuration.backendBaseUrl)

    val authClient: BackendApi
        get() = authClientOf(configuration.backendBaseUrl, userId)
}