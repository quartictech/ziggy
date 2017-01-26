package io.quartic.common.core

import java.security.*
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.RSAKeyGenParameterSpec


object SignatureUtils {
    fun verify(publicKey: PublicKey, data: ByteArray, signature: ByteArray): Boolean {
        val verificationFunction = Signature.getInstance("SHA256withRSA")
        verificationFunction.initVerify(publicKey)
        verificationFunction.update(data)

            return (verificationFunction.verify(signature))

    }

    fun verifyEC(publicKey: PublicKey, data: ByteArray, signature: ByteArray): Boolean {
        val verificationFunction = Signature.getInstance("SHA256withECDSA")
        verificationFunction.initVerify(publicKey)
        verificationFunction.update(data)
        return (verificationFunction.verify(signature))
    }

    fun sign(privateKey: PrivateKey, data: ByteArray): ByteArray {
        val s = Signature.getInstance("SHA256withRSA")
        s.initSign(privateKey)
        s.update(data)
        return s.sign()
    }

    fun generateRSAKeyPair(): KeyPair? {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        val algorithmParameterSpec = RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F0)
        return generateKey(keyPairGenerator, algorithmParameterSpec)
    }

    fun generateKey(keyPairGenerator: KeyPairGenerator, algorithmParameterSpec: AlgorithmParameterSpec): KeyPair? {
        try {
            keyPairGenerator.initialize(algorithmParameterSpec)
            return keyPairGenerator.generateKeyPair()
        } catch (e: Exception) {
            throw RuntimeException("Could not generate key pair", e) // TODO: what's a better error-handling approach?
        }
    }
}
