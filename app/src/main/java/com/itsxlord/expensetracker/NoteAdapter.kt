package com.itsxlord.expensetracker

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.itsxlord.expensetracker.data.NoteWithItems
import com.itsxlord.expensetracker.databinding.ItemExpenseNoteBinding
import java.text.NumberFormat
import java.util.*

class NoteAdapter(
    private val onNoteClick: (NoteWithItems) -> Unit,
    private val onNoteLongClick: (NoteWithItems) -> Unit
) : ListAdapter<NoteWithItems, NoteAdapter.NoteViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            ItemExpenseNoteBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NoteViewHolder(private val binding: ItemExpenseNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(noteWithItems: NoteWithItems) {
            val note = noteWithItems.note
            val items = noteWithItems.items
            
            binding.tvNoteTitle.text = note.title
            binding.cardNote.setCardBackgroundColor(note.color)
            
            // Adjust text color based on background brightness for contrast
            val isDark = ColorUtils.calculateLuminance(note.color) < 0.5
            val textColor = if (isDark) Color.WHITE else Color.BLACK
            val subTextColor = if (isDark) Color.argb(180, 255, 255, 255) else Color.argb(180, 0, 0, 0)
            
            binding.tvNoteTitle.setTextColor(textColor)
            binding.tvExpensePreview.setTextColor(subTextColor)
            
            val strokeColor = if (isDark) Color.argb(40, 255, 255, 255) else Color.argb(40, 0, 0, 0)
            binding.cardNote.strokeColor = strokeColor
            
            val currencyFormat = NumberFormat.getCurrencyInstance()
            
            if (items.isEmpty()) {
                binding.tvExpensePreview.text = "No expenses"
            } else {
                // Format preview with local currency
                val previewText = items.take(5).joinToString("\n") { item ->
                    "${item.description}: ${currencyFormat.format(item.amount)}"
                }
                binding.tvExpensePreview.text = if (items.size > 5) "$previewText\n..." else previewText
            }
            
            binding.root.setOnClickListener { onNoteClick(noteWithItems) }
            binding.root.setOnLongClickListener {
                onNoteLongClick(noteWithItems)
                true
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<NoteWithItems>() {
        override fun areItemsTheSame(oldItem: NoteWithItems, newItem: NoteWithItems): Boolean {
            return oldItem.note.id == newItem.note.id
        }

        override fun areContentsTheSame(oldItem: NoteWithItems, newItem: NoteWithItems): Boolean {
            return oldItem == newItem
        }
    }
}
