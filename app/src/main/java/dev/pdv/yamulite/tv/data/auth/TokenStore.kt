package dev.pdv.yamulite.tv.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore by preferencesDataStore("auth")

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val TOKEN = stringPreferencesKey("access_token")
        val REFRESH = stringPreferencesKey("refresh_token")
        val EXPIRES_AT = longPreferencesKey("expires_at")
        val DEVICE_ID = stringPreferencesKey("device_id")
    }

    val tokenFlow: Flow<String?> = context.authDataStore.data.map { it[Keys.TOKEN] }

    suspend fun saveToken(access: String, refresh: String?, expiresIn: Long) {
        context.authDataStore.edit { p ->
            p[Keys.TOKEN] = access
            if (refresh != null) p[Keys.REFRESH] = refresh
            p[Keys.EXPIRES_AT] = System.currentTimeMillis() + expiresIn * 1000L
        }
    }

    suspend fun clear() {
        context.authDataStore.edit { p ->
            p.remove(Keys.TOKEN)
            p.remove(Keys.REFRESH)
            p.remove(Keys.EXPIRES_AT)
        }
    }

    suspend fun getRefreshToken(): String? =
        context.authDataStore.data.first()[Keys.REFRESH]

    suspend fun getOrCreateDeviceId(): String {
        context.authDataStore.data.first()[Keys.DEVICE_ID]?.let { return it }
        val id = UUID.randomUUID().toString()
        context.authDataStore.edit { it[Keys.DEVICE_ID] = id }
        return id
    }
}
