package cash.atto.wallet.config

import cash.atto.wallet.BuildConfig

internal actual object PlatformNetworkOverrides {
    private fun env(key: String): String? = System.getenv(key)?.takeUnless { it.isBlank() }

    override val networkId: String? = env("ATTO_WALLET_NETWORK") ?: BuildConfig.WALLET_NETWORK
    override val gatekeeperEndpoint: String? = env("ATTO_GATEKEEPER_URL") ?: BuildConfig.GATEKEEPER_ENDPOINT
    override val nodeEndpoint: String? = env("ATTO_NODE_URL") ?: BuildConfig.NODE_ENDPOINT
    override val workerEndpoint: String? = env("ATTO_WORKER_URL") ?: BuildConfig.WORKER_ENDPOINT
    override val chainNetwork: String? = env("ATTO_CHAIN_NETWORK") ?: BuildConfig.CHAIN_NETWORK
    override val environment: String? = env("ATTO_ENVIRONMENT") ?: BuildConfig.WALLET_ENVIRONMENT
}
