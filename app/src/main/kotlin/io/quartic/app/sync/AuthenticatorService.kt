package io.quartic.app.sync

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class AuthenticatorService : Service() {
    companion object {
        const val TAG = "AuthenticatorService"
    }

    private var  authenticator: Authenticator? = null

    override fun onCreate() {
        Log.i(TAG, "creating authenticator service")
        authenticator = Authenticator(this)
    }

    override fun onBind(intent: Intent?): IBinder {
        return authenticator!!.iBinder
    }
}