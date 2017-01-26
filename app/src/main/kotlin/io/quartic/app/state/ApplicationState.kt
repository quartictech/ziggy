package io.quartic.app.state

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.Context.ACCOUNT_SERVICE
import io.quartic.app.ApplicationConfiguration
import io.quartic.app.api.BackendApi
import io.quartic.app.authHttpClient
import io.quartic.app.clientOf
import io.quartic.app.storage.Database
import okhttp3.OkHttpClient

class ApplicationState(val context: Context, val configuration: ApplicationConfiguration) {
    companion object {
        fun get(context: Context): ApplicationState {
            return ApplicationState(context, ApplicationConfiguration.load(context))
        }

        const val ACCOUNT_TYPE = "io.quartic.remote"
    }

    private val sharedPreferences = context.getSharedPreferences("tracker", 0)


    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    var userId: String?
        get() = sharedPreferences.getString("userId", null)
        set(userId) = sharedPreferences.edit()
                .putString("userId", userId)
                .apply()

    val client: BackendApi
        get() = clientOf(configuration.backendBaseUrl, OkHttpClient())

    val authClient: BackendApi
        get() = clientOf(configuration.backendBaseUrl, authHttpClient(userId!!))

    val database: Database
        get() = Database(context)

    val account: Account?
        get() {
            if (userId == null) {
                return null
            }
            val accountManager: AccountManager = context.getSystemService(ACCOUNT_SERVICE) as AccountManager;
            val account = Account(userId, ACCOUNT_TYPE)

            accountManager.addAccountExplicitly(account, null, null)
            return account
        }
    var lastSyncTime: Long
        get() = sharedPreferences.getLong("lastSyncTime", 0)
        set(timestamp) = sharedPreferences.edit()
                .putLong("lastSyncTime", timestamp)
                .apply()

    var lastAttemptedSyncTime: Long
        get() = sharedPreferences.getLong("lastAttemptedSyncTime", 0)
        set(timestamp) { sharedPreferences.edit()
                .putLong("lastAttemptedSyncTime", timestamp)
                .commit() }

}