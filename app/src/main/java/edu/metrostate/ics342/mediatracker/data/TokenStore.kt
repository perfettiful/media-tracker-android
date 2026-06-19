package edu.metrostate.ics342.mediatracker.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import edu.metrostate.ics342.mediatracker.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// holds the logged in session, survives app restarts
private val Context.authDataStore by preferencesDataStore(name = "auth")

class TokenStore(private val context: Context) {

    private val accessKey  = stringPreferencesKey("access_token")
    private val refreshKey = stringPreferencesKey("refresh_token")
    private val userKey    = stringPreferencesKey("user_json")

    private val json = Json { ignoreUnknownKeys = true }

    val accessToken: Flow<String?> = context.authDataStore.data.map { it[accessKey] }

    val currentUser: Flow<UserProfile?> = context.authDataStore.data.map { prefs ->
        prefs[userKey]?.let { json.decodeFromString<UserProfile>(it) }
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.authDataStore.edit { prefs ->
            prefs[accessKey]  = accessToken
            prefs[refreshKey] = refreshToken
        }
    }

    suspend fun saveUser(user: UserProfile) {
        context.authDataStore.edit { it[userKey] = json.encodeToString(user) }
    }

    suspend fun clear() {
        context.authDataStore.edit { it.clear() }
    }
}
