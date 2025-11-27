package cash.atto.wallet.config

@Suppress("UnsafeCastFromDynamic")
internal actual object PlatformNetworkOverrides {
    override val networkId: String? = readGlobal("ATTO_WALLET_NETWORK")
    override val gatekeeperEndpoint: String? = readGlobal("ATTO_GATEKEEPER_URL")
    override val nodeEndpoint: String? = readGlobal("ATTO_NODE_URL")
    override val workerEndpoint: String? = readGlobal("ATTO_WORKER_URL")
    override val chainNetwork: String? = readGlobal("ATTO_CHAIN_NETWORK")
    override val environment: String? = readGlobal("ATTO_ENVIRONMENT")

    private fun readGlobal(key: String): String? {
        val fromGlobal = js(
            """
            (typeof globalThis !== 'undefined' && typeof globalThis["$key"] === 'string') ? globalThis["$key"] : null
            """.trimIndent()
        )
        if (fromGlobal is String && fromGlobal.isNotBlank()) return fromGlobal

        val fromProcess = js(
            """
            (typeof process !== 'undefined' && process.env && typeof process.env["$key"] === 'string')
                ? process.env["$key"]
                : null
            """.trimIndent()
        )

        return (fromProcess as? String)?.takeUnless { it.isBlank() }
    }
}
