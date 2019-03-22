package com.unbi.connect

import android.app.Activity
import android.support.annotation.IdRes
import android.view.View

fun <T : View> Activity.bind(@IdRes idRes: Int): Lazy<T> {
    @Suppress("UNCHECKED_CAST")
    val myview = unsafeLazy {
        findViewById(idRes) as T
    }
    return myview

}

fun <T : View> View.bind(@IdRes idRes: Int): Lazy<T> {
    @Suppress("UNCHECKED_CAST")
    return unsafeLazy { findViewById(idRes) as T }
}

private fun <T> unsafeLazy(initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)

