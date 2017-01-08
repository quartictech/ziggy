package io.quartic.app.sync

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.Context.ACCOUNT_SERVICE

object AccountSingleton {
    fun getAccount(context: Context): Account {
        val accountManager: AccountManager = context.getSystemService(ACCOUNT_SERVICE) as AccountManager;
        val account = Account("dummy", "io.quartic.tracker")

        accountManager.addAccountExplicitly(account, null, null)
        return account
    }
}