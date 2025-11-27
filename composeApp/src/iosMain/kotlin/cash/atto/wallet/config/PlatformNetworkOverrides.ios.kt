package cash.atto.wallet.config

import platform.Foundation.NSProcessInfo

internal actual object PlatformNetworkOverrides {
    private val envVars = NSProcessInfo.processInfo.environment

    private fun read(key: String): String? = envVars[key] as? String

    override val networkId: String? = read("ATTO_WALLET_NETWORK")
    override val gatekeeperEndpoint: String? = read("ATTO_GATEKEEPER_URL")
    override val nodeEndpoint: String? = read("ATTO_NODE_URL")
    override val workerEndpoint: String? = read("ATTO_WORKER_URL")
    override val chainNetwork: String? = read("ATTO_CHAIN_NETWORK")
    override val environment: String? = read("ATTO_ENVIRONMENT")
}
