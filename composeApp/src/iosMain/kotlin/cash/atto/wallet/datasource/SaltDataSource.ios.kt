package cash.atto.wallet.datasource

import platform.Foundation.NSData
import platform.Foundation.create
import platform.Security.*
import kotlin.random.Random

actual class SaltDataSource {
    private val saltKey = "atto_wallet_salt"
    
    actual suspend fun get(): String {
        val existingSalt = getSaltFromKeychain()
        return existingSalt ?: generateAndSaveSalt()
    }
    
    private fun getSaltFromKeychain(): String? {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to saltKey,
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
    
    private fun generateAndSaveSalt(): String {
        val salt = Random.nextBytes(32).joinToString("") { "%02x".format(it) }
        saveSaltToKeychain(salt)
        return salt
    }
    
    private fun saveSaltToKeychain(salt: String) {
        // First, try to delete any existing entry
        deleteSaltFromKeychain()
        
        val saltData = salt.toByteArray().toNSData()
        
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to saltKey,
            kSecValueData to saltData,
            kSecAttrAccessible to kSecAttrAccessibleWhenUnlockedThisDeviceOnly
        )
        
        SecItemAdd(query.toCFDictionary(), null)
    }
    
    private fun deleteSaltFromKeychain() {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to saltKey
        )
        
        SecItemDelete(query.toCFDictionary())
    }
}

private fun ByteArray.toNSData(): NSData {
    return NSData.create(bytes = this.toCValues(), length = this.size)
}