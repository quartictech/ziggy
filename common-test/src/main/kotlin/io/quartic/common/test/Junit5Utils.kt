package io.quartic.common.test

import org.junit.jupiter.api.Assertions.assertThrows

inline fun <reified T : Throwable> assertThrows(crossinline block: () -> Unit): T {
    return assertThrows(T::class.java, { block() })
}