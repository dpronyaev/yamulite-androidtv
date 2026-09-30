package dev.pdv.yamulite.tv.data.auth

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthEvent {
    data class CodeReceived(
        val userCode: String,
        val verificationUrl: String,
        val expiresAt: Long,
    ) : AuthEvent()
    data object Authorized : AuthEvent()
    data class Failed(val message: String) : AuthEvent()
}

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val tokenStore: TokenStore,
    private val json: Json,
) {
    val tokenFlow: Flow<String?> = tokenStore.tokenFlow

    fun startAuth(): Flow<AuthEvent> = flow {
        val deviceId = tokenStore.getOrCreateDeviceId()
        val code = try {
            api.requestCode(
                clientId = CLIENT_ID,
                deviceId = deviceId,
                deviceName = DEVICE_NAME,
            )
        } catch (t: Throwable) {
            emit(AuthEvent.Failed("Не удалось получить код: ${t.message}"))
            return@flow
        }
        val deadline = System.currentTimeMillis() + code.expiresIn * 1000L
        emit(AuthEvent.CodeReceived(code.userCode, code.verificationUrl, deadline))

        var interval = code.interval.coerceAtLeast(1)
        while (System.currentTimeMillis() < deadline) {
            delay(interval * 1000L)
            val resp = try {
                api.pollToken(
                    grantType = "device_code",
                    code = code.deviceCode,
                    clientId = CLIENT_ID,
                    clientSecret = CLIENT_SECRET,
                )
            } catch (t: Throwable) {
                continue
            }
            if (resp.isSuccessful) {
                val token = resp.body() ?: continue
                tokenStore.saveToken(token.accessToken, token.refreshToken, token.expiresIn)
                emit(AuthEvent.Authorized)
                return@flow
            }
            val errBody = resp.errorBody()?.string().orEmpty()
            val err = runCatching { json.decodeFromString<OAuthError>(errBody) }.getOrNull()
            when (err?.error) {
                "authorization_pending" -> Unit
                "slow_down" -> interval += 5
                "expired_token" -> {
                    emit(AuthEvent.Failed("Время кода истекло"))
                    return@flow
                }
                "access_denied" -> {
                    emit(AuthEvent.Failed("Доступ отклонён пользователем"))
                    return@flow
                }
                else -> {
                    emit(AuthEvent.Failed(err?.errorDescription ?: err?.error ?: "HTTP ${resp.code()}"))
                    return@flow
                }
            }
        }
        emit(AuthEvent.Failed("Время кода истекло"))
    }

    suspend fun refreshToken(): Boolean {
        val rt = tokenStore.getRefreshToken() ?: return false
        return try {
            val resp = api.refreshToken("refresh_token", rt, CLIENT_ID, CLIENT_SECRET)
            if (!resp.isSuccessful) return false
            val body = resp.body() ?: return false
            tokenStore.saveToken(body.accessToken, body.refreshToken, body.expiresIn)
            true
        } catch (t: Throwable) { false }
    }

    suspend fun logout() = tokenStore.clear()

    companion object {
        const val CLIENT_ID = "23cabbbdc6cd418abb4b39c32c41195d"
        const val CLIENT_SECRET = "53bc75238f0c4d08a118e51fe9203300"
        const val DEVICE_NAME = "YaMuLite TV"
    }
}
