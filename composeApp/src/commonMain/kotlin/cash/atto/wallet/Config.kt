package cash.atto.wallet

object Config {
    const val DATABASE_VERSION = 2

    object Endpoints {
        const val ATTO_GATEKEEPER = "https://wallet-gatekeeper.application.atto.cash"
        const val ATTO_NODE = "https://wallet-node.application.atto.cash"
        const val ATTO_WORKER = "https://wallet-worker.application.atto.cash"

        const val ATTO_CASH_GATEKEEPER = "https://wallet-gatekeeper.dev.application.atto.cash"
        const val ATTO_CASH_NODE = "https://wallet-node.dev.application.atto.cash"
        const val ATTO_CASH_WORKER = "https://wallet-worker.dev.application.atto.cash"
    }

    /**
     * Legacy alias consumed by build scripts/tests. Points at the default Atto
     * gatekeeper endpoint so existing references continue to work.
     */
    const val ENDPOINT = Endpoints.ATTO_GATEKEEPER
}
