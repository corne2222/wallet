package cash.atto.wallet.datasource

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import cash.atto.commons.toHex
import cash.atto.commons.utils.SecureRandom
import cash.atto.wallet.util.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

actual class SaltDataSource(
    context: Context
) {
    private val dataStore = context.dataStore
    private val dataSourceScope = CoroutineScope(Dispatchers.IO)
    private var dataSourceJob: Job? = null

    actual suspend fun get(): String {
        val saltKey = stringPreferencesKey(SALT_KEY)
        val saltFlow = dataStore.data
            .map { preferences ->
                preferences[saltKey] ?: run {
                    val newSalt = SecureRandom.randomByteArray(16u).toHex()
                    // Store it asynchronously
                    CoroutineScope(Dispatchers.IO).launch {
                        dataStore.edit { prefs ->
                            prefs[saltKey] = newSalt
                        }
                    }
                    newSalt
                }
            }

        val saltChannel = Channel<String>()

        dataSourceJob?.cancel()
        dataSourceJob = dataSourceScope.launch {
            saltFlow.collect {
                saltChannel.send(it)
            }
        }

        return saltChannel.receive()
    }

    companion object {
        private const val SALT_KEY = "wallet_salt"
    }
}