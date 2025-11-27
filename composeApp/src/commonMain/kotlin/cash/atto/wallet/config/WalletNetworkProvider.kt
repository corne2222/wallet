package cash.atto.wallet.config

import cash.atto.commons.AttoNetwork

object WalletNetworkProvider {
    private val lock = Any()
    private var overrideConfig: WalletNetworkConfig? = null
    private var cachedConfig: WalletNetworkConfig? = null

    fun current(): WalletNetworkConfig = synchronized(lock) {
        overrideConfig ?: cachedConfig ?: resolve().also { cachedConfig = it }
    }

    fun reloadFromPlatform(): WalletNetworkConfig = synchronized(lock) {
        val resolved = resolve()
        cachedConfig = resolved
        resolved
    }

    fun override(config: WalletNetworkConfig?) = synchronized(lock) {
        overrideConfig = config
    }

    inline fun <T> withOverride(config: WalletNetworkConfig, block: () -> T): T {
        val previousOverride: WalletNetworkConfig?
        synchronized(lock) {
            previousOverride = overrideConfig
            overrideConfig = config
        }

        return try {
            block()
        } finally {
            synchronized(lock) {
                overrideConfig = previousOverride
            }
        }
    }

    private fun resolve(): WalletNetworkConfig {
        val selectedId = WalletNetworkId.from(PlatformNetworkOverrides.networkId) ?: WalletNetworkId.ATTO
        val base = WalletNetworkDefaults.byId(selectedId)
        val environment = WalletEnvironmentTier.from(PlatformNetworkOverrides.environment) ?: base.environment
        val chainNetwork = PlatformNetworkOverrides.chainNetwork
            ?.let { parseAttoNetwork(it) }
            ?: base.network

        val gatekeeper = PlatformNetworkOverrides.gatekeeperEndpoint?.takeUnless { it.isBlank() }
            ?: base.gatekeeperEndpoint
        val node = PlatformNetworkOverrides.nodeEndpoint?.takeUnless { it.isBlank() }
            ?: base.nodeEndpoint
        val worker = PlatformNetworkOverrides.workerEndpoint?.takeUnless { it.isBlank() }
            ?: base.workerEndpoint

        return base.copy(
            network = chainNetwork,
            environment = environment,
            gatekeeperEndpoint = gatekeeper,
            nodeEndpoint = node,
            workerEndpoint = worker,
        )
    }

    private fun parseAttoNetwork(raw: String): AttoNetwork? = runCatching {
        AttoNetwork.valueOf(raw.trim().uppercase())
    }.getOrNull()
}

internal expect object PlatformNetworkOverrides {
    val networkId: String?
    val gatekeeperEndpoint: String?
    val nodeEndpoint: String?
    val workerEndpoint: String?
    val chainNetwork: String?
    val environment: String?
}
