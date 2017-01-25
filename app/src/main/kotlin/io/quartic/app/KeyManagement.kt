package io.quartic.app

import android.content.Context
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProperties.*
import android.util.Log
import java.math.BigInteger
import java.security.spec.ECGenParameterSpec
import java.security.*
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.RSAKeyGenParameterSpec
import java.util.*
import javax.security.auth.x500.X500Principal


private const val KEY_ALIAS = "key"

//fun generateKeyPair23() {
//    if (isKeyPresent()) return
//    // TODO: validate that keys are being stored in hardware
//    try {
//        val kpg = KeyPairGenerator.getInstance(KEY_ALGORITHM_EC, "AndroidKeyStore")
//        kpg.initialize(ECGenParameterSpec("secp256r1"))
//        kpg.initialize(KeyGenParameterSpec.Builder(KEY_ALIAS, PURPOSE_SIGN)
//                .setDigests(DIGEST_SHA256)
//                .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
//                .build())
//        kpg.generateKeyPair()
//    } catch (e: Exception) {
//        throw RuntimeException("Could not generate key pair", e) // TODO: what's a better error-handling approach?
//    }
//}

fun listCryptoProviders() {
    val providers = Security.getProviders()
    for (provider in providers) {
        Log.i("CRYPTO", "provider: " + provider.name)
        val services = provider.services
        for (service in services) {
            Log.i("CRYPTO", "  algorithm: " + service.algorithm)
        }
    }
}

fun generateKeyPair(context: Context) {
    listCryptoProviders()
    generateKeyPair19(context)
}

fun generateKeyPair19(context: Context) {
    if (isKeyPresent()) return
    // TODO: validate that keys are being stored in hardware
    try {
        val kpg = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore")
        val cal = Calendar.getInstance()
        val now = cal.time
        cal.add(Calendar.YEAR, 1)
        val end = cal.time

        kpg.initialize(KeyPairGeneratorSpec.Builder(context)
                .setAlias(KEY_ALIAS)
                .setStartDate(now)
                .setEndDate(end)
                .setSerialNumber(BigInteger.ONE)
                .setSubject(X500Principal(
                        "CN=SomeDistinguishedName"))
                .build())
        kpg.generateKeyPair()
    } catch (e: Exception) {
        throw RuntimeException("Could not generate key pair", e) // TODO: what's a better error-handling approach?
    }
}


fun isKeyPresent(): Boolean {
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

