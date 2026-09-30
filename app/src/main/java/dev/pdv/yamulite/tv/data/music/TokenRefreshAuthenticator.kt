package dev.pdv.yamulite.tv.data.music

import dev.pdv.yamulite.tv.data.auth.AuthRepository
import dev.pdv.yamulite.tv.data.auth.TokenStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRefreshAuthenticator @Inject constructor(
    private val authRepo: AuthRepository,
    private val tokenStore: TokenStore,
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null
        val newToken = runBlocking {
            mutex.withLock {
                val currentToken = tokenStore.tokenFlow.first()
                val requestToken = response.request.header("Authorization")?.removePrefix("OAuth ")
                if (currentToken != null && currentToken != requestToken) {
                    return@withLock currentToken
                }
                val refreshed = authRepo.refreshToken()
                if (!refreshed) {
                    tokenStore.clear()
                    return@withLock null
                }
                tokenStore.tokenFlow.first()
            }
        } ?: return null
        return response.request.newBuilder()
            .header("Authorization", "OAuth $newToken")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var r = response.priorResponse
        while (r != null) { count++; r = r.priorResponse }
        return count
    }
}
