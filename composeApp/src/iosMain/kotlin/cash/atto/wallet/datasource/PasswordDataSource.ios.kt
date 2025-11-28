package cash.atto.wallet.datasource

import platform.Foundation.NSData
import platform.Foundation.create
import platform.Security.*

actual class PasswordDataSource {
    private val getPasswordKey = "atto_wallet_password"
    
    actual suspend fun getPassword(seed: String): String? {
        return getPasswordFromKeychain(seed)
    }
    
    actual suspend fun setPassword(seed: String, password: String) {
        savePasswordToKeychain(seed, password)
    }
    
    private fun getPasswordFromKeychain(seed: String): String? {
        val accountKey = "$getPasswordKey:${seed.hashCode()}"
        
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to accountKey,
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
    
    private fun savePasswordToKeychain(seed: String, password: String) {
        val accountKey = "$getPasswordKey:${seed.hashCode()}"
        
        // First, try to delete any existing entry
        deletePasswordFromKeychain(accountKey)
        
        val passwordData = password.toByteArray().toNSData()
        
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to accountKey,
            kSecValueData to passwordData,
            kSecAttrAccessible to kSecAttrAccessibleWhenUnlockedThisDeviceOnly
        )
        
        SecItemAdd(query.toCFDictionary(), null)
    }
    
    private fun deletePasswordFromKeychain(accountKey: String) {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to accountKey
        )
        
        SecItemDelete(query.toCFDictionary())
    }
}

private fun ByteArray.toNSData(): NSData {
    return NSData.create(bytes = this.toCValues(), length = this.size)
}