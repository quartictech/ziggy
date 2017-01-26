package io.quartic.common.core

import org.junit.Test


class SignatureUtilsShould {
    @Test
    fun generate_an_rsa_key() {
        SignatureUtils.generateRSAKeyPair()
    }
}
