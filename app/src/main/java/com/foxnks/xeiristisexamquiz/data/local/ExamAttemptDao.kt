package com.foxnks.xeiristisexamquiz.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object): defines the operations allowed on the "exam_attempts" table.
 * Room automatically generates the implementation of this interface.
 * DAO (Data Access Object): ορίζει τις λειτουργίες που επιτρέπονται πάνω στον πίνακα
 * "exam_attempts". Το Room παράγει αυτόματα την υλοποίηση αυτού του interface.
 */
@Dao
interface ExamAttemptDao {

    /**
     * Inserts a new exam attempt. Suspend function - must be called from a coroutine.
     * Εισάγει μία νέα απόπειρα τεστ. Suspend function - πρέπει να καλείται από coroutine.
     */
    @Insert
    suspend fun insert(attempt: ExamAttemptEntity)

    /**
     * Returns all attempts, most recent first, as a live data stream: if a new row is
     * inserted, anyone "listening" to this Flow is automatically updated.
     * Επιστρέφει όλες τις απόπειρες, πιο πρόσφατη πρώτη, ως ζωντανό ρεύμα δεδομένων:
     * αν προστεθεί νέα εγγραφή, όποιος "ακούει" αυτό το Flow ενημερώνεται αυτόματα.
     */
    @Query("SELECT * FROM exam_attempts ORDER BY timestampMillis DESC")
    fun getAllAttempts(): Flow<List<ExamAttemptEntity>>
}
