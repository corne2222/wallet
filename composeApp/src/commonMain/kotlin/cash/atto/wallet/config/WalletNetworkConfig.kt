package cash.atto.wallet.config

import cash.atto.commons.AttoNetwork
import cash.atto.wallet.Config

enum class WalletNetworkId {
    ATTO,
    ATTO_CASH;

    companion object {
        fun from(raw: String?): WalletNetworkId? {
            if (raw == null) return null
            return entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
        }
    }
}

enum class WalletEnvironmentTier {
    DEV,
    STAGING,
    PROD;

    companion object {
        fun from(raw: String?): WalletEnvironmentTier? {
            if (raw == null) return null
            return entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
        }
    }
}

data class WalletNetworkConfig(
    val id: WalletNetworkId,
    val network: AttoNetwork,
    val environment: WalletEnvironmentTier,
    val gatekeeperEndpoint: String,
    val nodeEndpoint: String,
    val workerEndpoint: String,
)

object WalletNetworkDefaults {
    val ATTO = WalletNetworkConfig(
        id = WalletNetworkId.ATTO,
        network = AttoNetwork.LIVE,
        environment = WalletEnvironmentTier.PROD,
        gatekeeperEndpoint = Config.Endpoints.ATTO_GATEKEEPER,
        nodeEndpoint = Config.Endpoints.ATTO_NODE,
        workerEndpoint = Config.Endpoints.ATTO_WORKER,
    )

    val ATTO_CASH = WalletNetworkConfig(
        id = WalletNetworkId.ATTO_CASH,
        network = AttoNetwork.DEV,
        environment = WalletEnvironmentTier.DEV,
        gatekeeperEndpoint = Config.Endpoints.ATTO_CASH_GATEKEEPER,
        nodeEndpoint = Config.Endpoints.ATTO_CASH_NODE,
        workerEndpoint = Config.Endpoints.ATTO_CASH_WORKER,
    )

    fun byId(id: WalletNetworkId): WalletNetworkConfig = when (id) {
        WalletNetworkId.ATTO -> ATTO
        WalletNetworkId.ATTO_CASH -> ATTO_CASH
    }
}
