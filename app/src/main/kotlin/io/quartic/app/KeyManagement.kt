package io.quartic.app

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.*
import java.security.spec.ECGenParameterSpec
import java.security.*

private const val KEY_ALIAS = "key"

fun generateKeyPair() {
    if (checkKeyExists()) return
    // TODO: validate that keys are being stored in hardware
    try {
        val kpg = KeyPairGenerator.getInstance(KEY_ALGORITHM_EC, "AndroidKeyStore")
        kpg.initialize(KeyGenParameterSpec.Builder(KEY_ALIAS, PURPOSE_SIGN)
                .setDigests(DIGEST_SHA256)
                .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                .build())
        kpg.generateKeyPair()
    } catch (e: Exception) {
        throw RuntimeException("Could not generate key pair", e) // TODO: what's a better error-handling approach?
    }
}

fun checkKeyExists(): Boolean {
    val ks = KeyStore.getInstance("AndroidKeyStore")
    ks.load(null)
    return ks.getEntry(KEY_ALIAS, null) != null
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

fun sign(data: ByteArray): ByteArray? {
    val ks = KeyStore.getInstance("AndroidKeyStore")
    ks.load(null)
    val entry = ks.getEntry(KEY_ALIAS, null) as? KeyStore.PrivateKeyEntry
            ?: throw RuntimeException("Not an instance of a PrivateKeyEntry")

    val s = Signature.getInstance("SHA256withECDSA")
    s.initSign(entry.privateKey)
    s.update(data)

    return s.sign()
}

