package app.theusual.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** What was in the cup, and so the colour of the ring it leaves on the page. */
enum class Drink(val ring: Long) {
    ESPRESSO(0xFF3D2213),
    FILTER(0xFF6B3B1F),
    CAPPUCCINO(0xFF93603A),
    FLAT_WHITE(0xFFA5774B),
    LATTE(0xFFC49C72),
    RAF(0xFFD4B688),
    COCOA(0xFF7A4A30),
    TEA(0xFF9C7A3A),
    OTHER(0xFF8C6A4E);

    companion object {
        fun of(s: String?) = entries.firstOrNull { it.name == s }
    }
}

/** The print on a café's sugar packet. */
enum class Pattern {
    STRIPES, DOTS, CHEVRON, BAND, SUN;

    companion object {
        fun of(s: String?) = entries.firstOrNull { it.name == s } ?: STRIPES
    }
}

/** Little stamps on the page: what the place is good for. */
enum class Tag {
    SOCKETS, QUIET, LAPTOP, WIFI, TERRACE, DOGS, BRUNCH, LATE;

    companion object {
        fun parse(s: String): Set<Tag> = s.split(',').mapNotNull { n -> entries.firstOrNull { it.name == n.trim() } }.toSet()
        fun join(t: Set<Tag>): String = t.sortedBy { it.ordinal }.joinToString(",") { it.name }
    }
}

/**
 * One café: a spread in the diary and a sugar packet on the board.
 *
 * [usual] is "the usual" in the owner's words ("flat white on oat, no sugar"), [usualDrink] the
 * kind of cup behind it. [spot] is where they like to sit, [barista] who makes it. A café [from] a
 * friend (with their [fromNote]) waits on the board as a dashed "want to go" packet until the
 * first visit. A café that shut keeps its page: [closedAt] and a last line, [farewell].
 */
@Entity(tableName = "cafes")
data class Cafe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val address: String = "",
    val city: String = "",
    /** The short line under the name on the packet: "by the bridge", "till 2 am". */
    val sub: String = "",
    val pattern: String = Pattern.STRIPES.name,
    /** An index into the packet colours. */
    val palette: Int = 0,
    val usual: String = "",
    val usualDrink: String = "",
    val spot: String = "",
    val barista: String = "",
    val tags: String = "",
    val story: String = "",
    /** The ink sketch of the front, made from a photo, in the app's sketches folder. */
    val sketch: String? = null,
    val from: String = "",
    val fromNote: String = "",
    val addedAt: Long = System.currentTimeMillis(),
    val closedAt: Long = 0,
    val farewell: String = "",
    val seed: Long = 0,
    /** Where it is on the map; null until it's put there. */
    val lat: Double? = null,
    val lon: Double? = null,
) {
    val patternOf: Pattern get() = Pattern.of(pattern)
    val tagsOf: Set<Tag> get() = Tag.parse(tags)
    val usualDrinkOf: Drink? get() = Drink.of(usualDrink)
    val closed: Boolean get() = closedAt > 0
    val onMap: Boolean get() = lat != null && lon != null
}

/**
 * One cup at one café: one ring on its page. [what] is the order in words, [with] who was there,
 * [note] the line written beside the ring. [windowMin] is the time spent with the phone face down.
 */
@Entity(tableName = "visits", indices = [Index("cafeId")])
data class Visit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cafeId: Long,
    val at: Long = System.currentTimeMillis(),
    val drink: String = Drink.OTHER.name,
    val what: String = "",
    val price: Int? = null,
    val with: String = "",
    val note: String = "",
    val windowMin: Int = 0,
) {
    val drinkOf: Drink get() = Drink.of(drink) ?: Drink.OTHER
}

@Dao
interface DiaryDao {
    @Query("SELECT * FROM cafes ORDER BY addedAt")
    fun observeCafes(): Flow<List<Cafe>>

    @Query("SELECT * FROM cafes ORDER BY addedAt")
    suspend fun cafes(): List<Cafe>

    @Query("SELECT * FROM cafes WHERE id = :id")
    suspend fun cafe(id: Long): Cafe?

    @Insert
    suspend fun insert(c: Cafe): Long

    @Update
    suspend fun update(c: Cafe)

    @Query("SELECT * FROM visits ORDER BY at")
    fun observeVisits(): Flow<List<Visit>>

    @Query("SELECT * FROM visits ORDER BY at")
    suspend fun visits(): List<Visit>

    @Query("SELECT * FROM visits WHERE id = :id")
    suspend fun visit(id: Long): Visit?

    @Insert
    suspend fun insert(v: Visit): Long

    @Update
    suspend fun update(v: Visit)

    @Query("DELETE FROM visits WHERE id = :id")
    suspend fun deleteVisit(id: Long)

    @Query("DELETE FROM visits WHERE cafeId = :id")
    suspend fun deleteVisits(id: Long)

    @Query("DELETE FROM cafes WHERE id = :id")
    suspend fun deleteCafe(id: Long)

    /** Torn out of the diary, with all its rings. */
    @Transaction
    suspend fun forget(id: Long) {
        deleteVisits(id)
        deleteCafe(id)
    }
}

@Database(entities = [Cafe::class, Visit::class], version = 2, exportSchema = true, autoMigrations = [AutoMigration(from = 1, to = 2)])
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun dao(): DiaryDao

    companion object {
        fun create(context: Context): DiaryDatabase =
            Room.databaseBuilder(context.applicationContext, DiaryDatabase::class.java, "theusual.db").build()
    }
}
