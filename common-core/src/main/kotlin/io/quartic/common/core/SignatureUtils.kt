package io.quartic.common.core

import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
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

    fun rsaKeyPairGenerator(): KeyPairGenerator {
        val kpg = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F0))
        return kpg
    }

    fun ecKeyPairGenerator(): KeyPairGenerator {
        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(ECGenParameterSpec("secp256r1"))
        return kpg
    }
}
