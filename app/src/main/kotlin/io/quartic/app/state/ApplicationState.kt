package io.quartic.app.state

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.Context.ACCOUNT_SERVICE
import io.quartic.app.ApplicationConfiguration
import io.quartic.app.api.BackendApi
import io.quartic.app.authHttpClient
import io.quartic.app.clientOf
import io.quartic.app.sensors.Database
import okhttp3.OkHttpClient

class ApplicationState(val context: Context, val configuration: ApplicationConfiguration) {
    companion object {
        fun get(context: Context): ApplicationState {
            return ApplicationState(context, ApplicationConfiguration.load(context))
        }
    }

    private val sharedPreferences = context.getSharedPreferences("tracker", 0)

    var userId: String
        get() = sharedPreferences.getString("userId", null)
        set(userId) = sharedPreferences.edit()
                .putString("userId", userId)
                .apply()

    val client: BackendApi
        get() = clientOf(configuration.backendBaseUrl, OkHttpClient())

    val authClient: BackendApi
        get() = clientOf(configuration.backendBaseUrl, authHttpClient(userId))

    val database: Database
        get() = Database(context)

    val account: Account
        get() {
            val accountManager: AccountManager = context.getSystemService(ACCOUNT_SERVICE) as AccountManager;
            val account = Account("dummy", "io.quartic.tracker")

            accountManager.addAccountExplicitly(account, null, null)
            return account
        }
}