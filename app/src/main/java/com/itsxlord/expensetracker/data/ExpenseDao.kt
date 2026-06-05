package com.itsxlord.expensetracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Transaction
    @Query("SELECT * FROM expense_notes ORDER BY createdAt DESC")
    fun getNotesWithItems(): Flow<List<NoteWithItems>>

    @Query("SELECT * FROM expense_notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<ExpenseNote>>

    @Query("SELECT * FROM expense_notes WHERE id = :noteId")
    fun getNoteById(noteId: Long): Flow<ExpenseNote>

    @Insert
    suspend fun insertNote(note: ExpenseNote): Long

    @Update
    suspend fun updateNote(note: ExpenseNote)

    @Delete
    suspend fun deleteNote(note: ExpenseNote)

    @Query("SELECT * FROM expense_items WHERE noteId = :noteId ORDER BY timestamp DESC")
    fun getItemsForNote(noteId: Long): Flow<List<ExpenseItem>>

    @Insert
    suspend fun insertItem(item: ExpenseItem)

    @Delete
    suspend fun deleteItem(item: ExpenseItem)
}
