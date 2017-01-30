package io.quartic.app.state

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.Context.ACCOUNT_SERVICE
import android.content.SharedPreferences
import android.util.Log
import io.quartic.app.ApplicationConfiguration
import io.quartic.app.api.BackendApi
import io.quartic.app.authHttpClient
import io.quartic.app.clientOf
import io.quartic.app.storage.Database
import io.quartic.app.tag
import okhttp3.OkHttpClient

class ApplicationState(val context: Context, val configuration: ApplicationConfiguration) {
    companion object {
        fun get(context: Context): ApplicationState {
            return ApplicationState(context, ApplicationConfiguration.load(context))
        }

        const val ACCOUNT_TYPE = "io.quartic.remote"
        const val PREFERENCES_NAME = "remote"
        val TAG by tag()
    }

    private val sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, 0)

    private fun <R> checkedEdit(block: SharedPreferences.Editor.() -> R) {
        val editor = sharedPreferences.edit()
        editor.block()

        // commit() applies edits synchronously and returns an exit status
        if (!editor.commit()) {
            Log.e(TAG, "exception while committing to shared preferences")
        }
    }

    fun clear() {
        checkedEdit { clear() }
    }

    var userId: String?
        get() = sharedPreferences.getString("userId", null)
        set(userId) = checkedEdit { this.putString("userId", userId) }

    val client: BackendApi
        get() = clientOf(configuration.backendBaseUrl, OkHttpClient())

    val authClient: BackendApi
        get() = clientOf(configuration.backendBaseUrl, authHttpClient(userId!!))

    val database: Database
        get() = Database(context)

    val account: Account?
        get() {
            val accountManager = context.getSystemService(ACCOUNT_SERVICE) as AccountManager
            val account = Account("Quartic Remote Account", ACCOUNT_TYPE)

            accountManager.addAccountExplicitly(account, null, null)
            return account
        }

    var lastSyncTime: Long
        get() = sharedPreferences.getLong("lastSyncTime", 0)
        set(timestamp) = checkedEdit { putLong("lastSyncTime", timestamp) }

    var lastAttemptedSyncTime: Long
        get() = sharedPreferences.getLong("lastAttemptedSyncTime", 0)
        set(timestamp) = checkedEdit { putLong("lastAttemptedSyncTime", timestamp) }

    var numConsecutiveSyncAuthFailures: Int
        get() = sharedPreferences.getInt("numConsecutiveSyncAuthFailures", 0)
        set(num) = checkedEdit { putInt("numConsecutiveSyncAuthFailures", num) }
}