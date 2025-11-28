package cash.atto.wallet.datasource

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import cash.atto.wallet.Config

@Database(
    entities = [AccountEntryIos::class, WorkIos::class],
    version = Config.DATABASE_VERSION
)
abstract class AppDatabaseIos : RoomDatabase(), AppDatabase, DB {
    abstract override fun accountEntryDao(): AccountEntryDaoIos
    abstract override fun workDao(): WorkDaoIos

    override fun clearAllTables() {
        super.clearAllTables()
    }
}

interface DB {
    fun clearAllTables() {}
}

@Dao
interface AccountEntryDaoIos : AccountEntryDao {
    @Query(
        "SELECT entry from accountEntries " +
                "WHERE publicKey = :publicKey " +
                "ORDER BY height DESC LIMIT 1"
    )
    override suspend fun last(publicKey: ByteArray): ByteArray?

    @Query(
        "SELECT entry from accountEntries " +
                "WHERE publicKey = :publicKey " +
                "ORDER BY height DESC"
    )
    override suspend fun list(publicKey: ByteArray): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entry: AccountEntryIos)

    override suspend fun save(entry: AccountEntry) = save(entry as AccountEntryIos)
}

@Dao
interface WorkDaoIos : WorkDao {
    @Query("SELECT * FROM work ORDER BY value LIMIT 1")
    override suspend fun get(): WorkIos?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(work: WorkIos)

    override suspend fun set(work: Work) = set(work as WorkIos)

    @Query("DELETE FROM work")
    override suspend fun clear()
}

@Entity(tableName = "accountEntries")
data class AccountEntryIos(
    @PrimaryKey
    override val hash: ByteArray,
    override val publicKey: ByteArray,
    override val height: Long,
    override val entry: String
) : AccountEntry

@Entity(tableName = "work")
data class WorkIos(
    @PrimaryKey
    override val publicKey: ByteArray,
    override val value: ByteArray
) : Work

actual fun createAccountEntry(
    hash: ByteArray,
    publicKey: ByteArray,
    height: Long,
    entry: String
): AccountEntry = AccountEntryIos(
    hash = hash,
    publicKey = publicKey,
    height = height,
    entry = entry
)

actual fun createWork(
    publicKey: ByteArray,
    value: ByteArray
): Work = WorkIos(
    publicKey = publicKey,
    value = value
)