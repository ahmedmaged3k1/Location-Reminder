package com.udacity.project4.utils

import androidx.test.espresso.idling.CountingIdlingResource

object EspressoUtils {
    @JvmField
    val countingIdlingResource = CountingIdlingResource("GLOBAL")

}

inline fun <T> wrapEspressoIdlingResource(function: () -> T): T {
    // Espresso does not work well with coroutines yet. See
    // https://github.com/Kotlin/kotlinx.coroutines/issues/982
    EspressoUtils.countingIdlingResource.increment() // Set app as busy.
    return try {
        function()
    } finally {
        EspressoUtils.countingIdlingResource.decrement()
        // Set app as idle.
    }
}