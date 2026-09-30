package dev.pdv.yamulite.tv.data.music

import dev.pdv.yamulite.tv.data.auth.TokenStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    tokenStore: TokenStore,
) : Interceptor {

    // Kept current reactively so intercept() never blocks a dispatcher thread on a
    // DataStore read for every request — only the very first token read is synchronous.
    @Volatile private var currentToken: String? = runBlocking { tokenStore.tokenFlow.first() }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch { tokenStore.tokenFlow.collect { currentToken = it } }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request().newBuilder()
            .header("Accept-Language", "ru")
            .header("User-Agent", "YaMuLiteTV/0.1 (Android TV)")
            .apply { currentToken?.let { header("Authorization", "OAuth $it") } }
            .build()
        return chain.proceed(req)
    }
}
