package io.quartic.app

import kotlin.reflect.companionObject

// See http://stackoverflow.com/a/34462577/129570
fun <R : Any> R.tag(): Lazy<String> {
    return lazy { unwrapCompanionClass(this.javaClass).name }
}

fun <T: Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
    return if (ofClass.enclosingClass != null && ofClass.enclosingClass.kotlin.companionObject?.java == ofClass) {
        ofClass.enclosingClass
    } else {
        ofClass
    }
}

