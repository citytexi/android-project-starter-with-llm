package com.example.starter.feature.sample

/**
 * Why the last refresh failed. The ViewModel never holds display text: [SampleScreen] maps each
 * case to a string resource, so the message follows the device language.
 */
sealed interface SampleError {
    /** No connection or a timeout (`IOException` from the data layer). */
    data object Network : SampleError

    /** The server answered with an HTTP error status (`HttpStatusException`). */
    data object Server : SampleError

    /** Anything else. */
    data object Unknown : SampleError
}
