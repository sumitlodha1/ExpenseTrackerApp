package com.itsxlord.expensetracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Transaction
    @Query("SELECT * FROM expense_notes WHERE isArchived = 0 AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getActiveNotesWithItems(): Flow<List<NoteWithItems>>

    @Transaction
    @Query("SELECT * FROM expense_notes WHERE isArchived = 1 AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getArchivedNotesWithItems(): Flow<List<NoteWithItems>>

    @Transaction
    @Query("SELECT * FROM expense_notes WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedNotesWithItems(): Flow<List<NoteWithItems>>

    @Query("SELECT * FROM expense_notes WHERE id = :noteId")
    fun getNoteById(noteId: Long): Flow<ExpenseNote?>

    @Insert
    suspend fun insertNote(note: ExpenseNote): Long

    @Update
    suspend fun updateNote(note: ExpenseNote)

    @Delete
    suspend fun deleteNotePermanently(note: ExpenseNote)

    @Query("DELETE FROM expense_notes WHERE isDeleted = 1 AND deletedAt < :threshold")
    suspend fun deleteOldNotes(threshold: Long)

    @Query("SELECT * FROM expense_items WHERE noteId = :noteId ORDER BY timestamp DESC")
    fun getItemsForNote(noteId: Long): Flow<List<ExpenseItem>>

    @Insert
    suspend fun insertItem(item: ExpenseItem)

    @Delete
    suspend fun deleteItem(item: ExpenseItem)
}
