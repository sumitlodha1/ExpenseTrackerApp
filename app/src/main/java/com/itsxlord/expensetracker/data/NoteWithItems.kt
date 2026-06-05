package com.itsxlord.expensetracker.data

import androidx.room.Embedded
import androidx.room.Relation

data class NoteWithItems(
    @Embedded val note: ExpenseNote,
    @Relation(
        parentColumn = "id",
        entityColumn = "noteId"
    )
    val items: List<ExpenseItem>
)
