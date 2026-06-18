package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {
    // folders
    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<Folder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: Folder): Long

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteFolder(id: Long)

    // textbooks
    @Query("SELECT * FROM textbooks WHERE folderId = :folderId ORDER BY title ASC")
    fun getTextbooksInFolder(folderId: Long): Flow<List<Textbook>>

    @Query("SELECT * FROM textbooks ORDER BY id DESC")
    fun getAllTextbooks(): Flow<List<Textbook>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTextbook(textbook: Textbook): Long

    @Update
    suspend fun updateTextbook(textbook: Textbook)

    @Query("DELETE FROM textbooks WHERE id = :id")
    suspend fun deleteTextbook(id: Long)

    // drawings
    @Query("SELECT * FROM drawings WHERE folderId = :folderId ORDER BY lastModified DESC")
    fun getDrawingsInFolder(folderId: Long): Flow<List<Drawing>>

    @Query("SELECT * FROM drawings WHERE id = :id")
    suspend fun getDrawingById(id: Long): Drawing?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrawing(drawing: Drawing): Long

    @Update
    suspend fun updateDrawing(drawing: Drawing)

    @Query("UPDATE drawings SET strokesJson = :strokesJson, lastModified = :lastModified, backgroundType = :bgType WHERE id = :id")
    suspend fun saveDrawingStrokes(id: Long, strokesJson: String, bgType: String, lastModified: Long = System.currentTimeMillis())

    @Query("DELETE FROM drawings WHERE id = :id")
    suspend fun deleteDrawing(id: Long)

    // tasks
    @Query("SELECT * FROM tasks ORDER BY deadline ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE folderId = :folderId ORDER BY deadline ASC")
    fun getTasksInFolder(folderId: Long): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateTaskStatus(id: Long, isCompleted: Boolean)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    // flashcards
    @Query("SELECT * FROM flashcards WHERE folderId = :folderId ORDER BY createdAt DESC")
    fun getFlashcardsInFolder(folderId: Long): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards ORDER BY createdAt DESC")
    fun getAllFlashcards(): Flow<List<Flashcard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: Flashcard): Long

    @Update
    suspend fun updateFlashcard(flashcard: Flashcard)

    @Query("DELETE FROM flashcards WHERE id = :id")
    suspend fun deleteFlashcard(id: Long)
}

@Database(
    entities = [Folder::class, Textbook::class, Drawing::class, Task::class, Flashcard::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val studyDao: StudyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "studyos_pro_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
