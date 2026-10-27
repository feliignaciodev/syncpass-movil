package com.example.syncpassapp.core.network.remote

import com.example.syncpassapp.core.datastore.TokenDataStore
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val tokenDataStore: TokenDataStore
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.url.encodedPath.contains("/auth/login")) {
            runBlocking { tokenDataStore.clearTokens() }
            return null
        }

        runBlocking { tokenDataStore.clearTokens() }
        return null
    }
}
