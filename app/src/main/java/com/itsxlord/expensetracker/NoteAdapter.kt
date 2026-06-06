package com.itsxlord.expensetracker

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
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
            // Using a neutral color since color feature is removed
            binding.cardNote.setCardBackgroundColor(Color.WHITE)
            
            val textColor = Color.BLACK
            val subTextColor = Color.argb(180, 0, 0, 0)
            
            binding.tvNoteTitle.setTextColor(textColor)
            binding.tvExpensePreview.setTextColor(subTextColor)
            
            // Set border color
            binding.cardNote.strokeColor = Color.argb(40, 0, 0, 0)
            
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
