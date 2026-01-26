package np.ict.mad.advanced.data


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


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
    @PrimaryKey(autoGenerate = true) val Id: Long = 0,
    val userId: Long,
    val score: Int,
    val timestamp: Long
)

