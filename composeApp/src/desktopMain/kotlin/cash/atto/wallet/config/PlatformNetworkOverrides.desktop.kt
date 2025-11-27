package cash.atto.wallet.config

internal actual object PlatformNetworkOverrides {
    private fun propertyOrEnv(key: String): String? {
        val env = System.getenv(key)?.takeUnless { it.isBlank() }
        if (env != null) return env
        return System.getProperty(key)?.takeUnless { it.isBlank() }
    }

    override val networkId: String? = propertyOrEnv("ATTO_WALLET_NETWORK")
    override val gatekeeperEndpoint: String? = propertyOrEnv("ATTO_GATEKEEPER_URL")
    override val nodeEndpoint: String? = propertyOrEnv("ATTO_NODE_URL")
    override val workerEndpoint: String? = propertyOrEnv("ATTO_WORKER_URL")
    override val chainNetwork: String? = propertyOrEnv("ATTO_CHAIN_NETWORK")
    override val environment: String? = propertyOrEnv("ATTO_ENVIRONMENT")
}
