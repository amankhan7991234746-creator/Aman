package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

// Folder representing subjects like "Physics", "Chemistry", "Biology" etc.
@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String = "📁", // Emoji representation
    val createdAt: Long = System.currentTimeMillis()
)

// Textbook / reading material
@Entity(tableName = "textbooks")
data class Textbook(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folderId: Long,
    val title: String,
    val author: String = "Unknown Author",
    val content: String, // Full text content for reading and AI tutor OCR context
    val totalPages: Int = 5,
    val currentPage: Int = 1,
    val rating: Float = 0.0f
)

// Drawing note (handwritten note in drawing canvas)
@Entity(tableName = "drawings")
data class Drawing(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folderId: Long,
    val name: String,
    val lastModified: Long = System.currentTimeMillis(),
    val strokesJson: String = "[]", // Serialized list of Stroke
    val backgroundType: String = "LINED" // BLANK, LINED, GRID
)

// Stroke representations for the handwritten Canvas
data class StrokePoint(val x: Float, val y: Float, val pressure: Float = 1f)
data class Stroke(
    val points: List<StrokePoint>,
    val color: Int,
    val width: Float,
    val isHighlighter: Boolean = false
)

// Task checklist
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folderId: Long,
    val title: String,
    val deadline: Long = System.currentTimeMillis() + 86400000,
    val isCompleted: Boolean = false,
    val priority: String = "MEDIUM" // HIGH, MEDIUM, LOW
)

// Study Flashcard
@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folderId: Long,
    val front: String,
    val back: String,
    val isKnown: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// Room Type Converters for List<Stroke> and others
class AppTypeConverters {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val strokeListType = Types.newParameterizedType(List::class.java, Stroke::class.java)
    private val strokeListAdapter = moshi.adapter<List<Stroke>>(strokeListType)

    @TypeConverter
    fun fromStrokeList(strokes: List<Stroke>?): String {
        return strokes?.let { strokeListAdapter.toJson(it) } ?: "[]"
    }

    @TypeConverter
    fun toStrokeList(json: String?): List<Stroke> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            strokeListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
