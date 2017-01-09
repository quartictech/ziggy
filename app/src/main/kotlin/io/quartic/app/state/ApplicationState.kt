package io.quartic.app.state

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.Context.ACCOUNT_SERVICE
import io.quartic.app.ApplicationConfiguration
import io.quartic.app.api.BackendApi
import io.quartic.app.authClientOf
import io.quartic.app.clientOf
import io.quartic.app.sensors.Database

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

    val database: Database
        get() = Database.getInstance(context)

    val account: Account
        get() {
            val accountManager: AccountManager = context.getSystemService(ACCOUNT_SERVICE) as AccountManager;
            val account = Account("dummy", "io.quartic.tracker")

            accountManager.addAccountExplicitly(account, null, null)
            return account
        }
}