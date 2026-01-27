package np.ict.mad.advanced.data


import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query


// Score Entity
@Entity(
    tableName = "scores",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val score: Int,
    val timestamp: Long
)

// Score Dao
@Dao
interface ScoreDao{
    @Insert
    suspend fun insertScore(score: ScoreEntity): Long

    // for Personal best of a user
    @Query("SELECT MAX(score) FROM scores WHERE userId = :userId")
    suspend fun getPersonalBest(userId: Long): Int?

    // Best score per user (leaderboard)
    @Query("""
        SELECT u.username AS username, MAX(s.score) AS bestScore
        FROM users u
        LEFT JOIN scores s ON u.userId = s.userId
        GROUP BY u.userId
        ORDER BY bestScore DESC
    """)
    suspend fun getLeaderboard(): List<LeaderboardRow>
}

data class LeaderboardRow(
    val username: String,
    val bestScore: Int?
)

