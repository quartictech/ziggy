package io.quartic.app

import android.content.Context
import android.security.KeyPairGeneratorSpec
import io.quartic.common.core.SignatureUtils
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PublicKey
import java.util.*
import javax.security.auth.x500.X500Principal

private const val KEY_ALIAS = "key"

fun generateKeyPair(context: Context) {
    if (isKeyPresent()) return
    // TODO: validate that keys are being stored in hardware
    val kpg = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore")
    val cal = Calendar.getInstance()
    val now = cal.time
    cal.add(Calendar.YEAR, 1)
    val end = cal.time

    val parameterSpec = KeyPairGeneratorSpec.Builder(context)
            .setAlias(KEY_ALIAS)
            .setStartDate(now)
            .setEndDate(end)
            .setSerialNumber(BigInteger.ONE)
            // TODO: does this need to be something proper?
            .setSubject(X500Principal("CN=SomeDistinguishedName"))
            .build()
    SignatureUtils.generateKey(kpg, parameterSpec)
}


fun isKeyPresent(): Boolean {
    val ks = KeyStore.getInstance("AndroidKeyStore")
    ks.load(null)
    return ks.getEntry(KEY_ALIAS, null) != null
}

fun deleteKey() {
    val ks = KeyStore.getInstance("AndroidKeyStore")
    ks.load(null)
    ks.deleteEntry(KEY_ALIAS)
}

// TODO: what's a better error-handling approach?
val publicKey: PublicKey
    get() {
        try {
            val ks = KeyStore.getInstance("AndroidKeyStore")
            ks.load(null)
            return ks.getCertificate(KEY_ALIAS).publicKey
        } catch (e: Exception) {
            throw RuntimeException("Could not acquire public key", e)
        }
    }

fun sign(data: ByteArray): ByteArray {
    val ks = KeyStore.getInstance("AndroidKeyStore")
    ks.load(null)
    val entry = ks.getEntry(KEY_ALIAS, null) as? KeyStore.PrivateKeyEntry
            ?: throw RuntimeException("Not an instance of a PrivateKeyEntry")
    return SignatureUtils.sign(entry.privateKey, data)
}

