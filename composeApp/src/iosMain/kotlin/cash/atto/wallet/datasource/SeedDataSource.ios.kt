package cash.atto.wallet.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Security.*
import kotlinx.coroutines.flow.flow

actual class SeedDataSource {
    private val seedKey = "atto_wallet_seed"
    
    actual val seed: Flow<String?> = flow {
        emit(getSeedFromKeychain())
    }
    
    actual suspend fun setSeed(seed: String) {
        saveSeedToKeychain(seed)
    }
    
    actual suspend fun clearSeed() {
        deleteSeedFromKeychain()
    }
    
    private fun getSeedFromKeychain(): String? {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to seedKey,
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne
        )
        
        var result: CFTypeRef? = null
        val status = SecItemCopyMatching(query.toCFDictionary(), result.ptr)
        
        return if (status == errSecSuccess) {
            val data = result as? NSData
            data?.let { 
                String(it.toByteArray())
            }
        } else null
    }
    
    private fun saveSeedToKeychain(seed: String) {
        // First, try to delete any existing entry
        deleteSeedFromKeychain()
        
        val seedData = seed.toByteArray().toNSData()
        
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to seedKey,
            kSecValueData to seedData,
            kSecAttrAccessible to kSecAttrAccessibleWhenUnlockedThisDeviceOnly
        )
        
        SecItemAdd(query.toCFDictionary(), null)
    }
    
    private fun deleteSeedFromKeychain() {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to seedKey
        )
        
        SecItemDelete(query.toCFDictionary())
    }
}

private fun ByteArray.toNSData(): NSData {
    return NSData.create(bytes = this.toCValues(), length = this.size)
}