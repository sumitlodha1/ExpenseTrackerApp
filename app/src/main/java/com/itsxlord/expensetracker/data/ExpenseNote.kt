package com.itsxlord.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_notes")
data class ExpenseNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val color: Int = 0xFFFFFFFF.toInt(), // Default white
    val createdAt: Long = System.currentTimeMillis()
)
