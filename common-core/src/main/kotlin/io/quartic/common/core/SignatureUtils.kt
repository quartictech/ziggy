package io.quartic.common.core

import java.security.*
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.ECGenParameterSpec
import java.security.spec.RSAKeyGenParameterSpec


object SignatureUtils {
    const val SIGNING_ALGORITHM = "SHA256withRSA"
    const val ALGORITHM = "RSA"

    fun verify(publicKey: PublicKey, data: ByteArray, signature: ByteArray): Boolean {
        val verificationFunction = Signature.getInstance(SIGNING_ALGORITHM)
        verificationFunction.initVerify(publicKey)
        verificationFunction.update(data)
        return (verificationFunction.verify(signature))
    }

    fun sign(privateKey: PrivateKey, data: ByteArray): ByteArray {
        val s = Signature.getInstance(SIGNING_ALGORITHM)
        s.initSign(privateKey)
        s.update(data)
        return s.sign()
    }

    fun generateRSAKeyPair() = generateKeyPair(
            KeyPairGenerator.getInstance("RSA"),
            RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F0)
    )

    fun generateECKeyPair() = generateKeyPair(
            KeyPairGenerator.getInstance("EC"),
            ECGenParameterSpec("secp256r1")
    )

    private fun generateKeyPair(keyPairGenerator: KeyPairGenerator, algorithmParameterSpec: AlgorithmParameterSpec): KeyPair {
        try {
            keyPairGenerator.initialize(algorithmParameterSpec)
            return keyPairGenerator.generateKeyPair()
        } catch (e: Exception) {
            throw RuntimeException("Could not generate key pair", e) // TODO: what's a better error-handling approach?
        }
    }
}
