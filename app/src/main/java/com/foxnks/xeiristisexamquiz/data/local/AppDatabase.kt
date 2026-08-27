package com.foxnks.xeiristisexamquiz.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The app's Room database. Currently holds a single table (ExamAttemptEntity) for the
 * final-exam attempt history.
 * Η βάση δεδομένων Room της εφαρμογής. Περιέχει προς το παρόν έναν μόνο πίνακα
 * (ExamAttemptEntity) για το ιστορικό τελικών τεστ.
 */
@Database(entities = [ExamAttemptEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun examAttemptDao(): ExamAttemptDao

    companion object {
        // @Volatile: guarantees all threads always see the most up-to-date value of instance
        // @Volatile: εγγυάται ότι όλα τα threads βλέπουν πάντα την πιο πρόσφατη τιμή του instance
        @Volatile
        private var instance: AppDatabase? = null

        /**
         * Returns the single (singleton) database instance, creating it if it doesn't
         * already exist. The "double-checked locking" pattern avoids accidentally creating
         * two instances if two threads request it at the same time.
         * Επιστρέφει το μοναδικό (singleton) instance της βάσης, δημιουργώντας το αν δεν
         * υπάρχει ήδη. Το "double-checked locking" (έλεγχος-κλείδωμα-έλεγχος) αποφεύγει
         * να φτιαχτούν κατά λάθος δύο instances αν δύο threads το ζητήσουν ταυτόχρονα.
         */
        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "xeiristis_exam_quiz.db"
                ).build().also { instance = it }
            }
    }
}
