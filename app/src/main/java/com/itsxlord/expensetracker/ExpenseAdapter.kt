package com.itsxlord.expensetracker

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.itsxlord.expensetracker.data.ExpenseItem
import com.itsxlord.expensetracker.databinding.ItemExpenseBinding
import java.text.NumberFormat
import java.util.*

class ExpenseAdapter(private val onItemLongClick: (ExpenseItem) -> Unit) : 
    ListAdapter<ExpenseItem, ExpenseAdapter.ExpenseViewHolder>(DiffCallback) {

    private var textColor: Int = Color.BLACK

    fun setTextColor(color: Int) {
        this.textColor = color
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        return ExpenseViewHolder(
            ItemExpenseBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position), textColor)
    }

    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ExpenseItem, color: Int) {
            val currencyFormat = NumberFormat.getCurrencyInstance()
            binding.tvExpenseAmount.text = currencyFormat.format(item.amount)
            binding.tvExpenseDescription.text = item.description
            
            binding.tvExpenseAmount.setTextColor(color)
            binding.tvExpenseDescription.setTextColor(color)
            
            binding.root.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ExpenseItem>() {
        override fun areItemsTheSame(oldItem: ExpenseItem, newItem: ExpenseItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ExpenseItem, newItem: ExpenseItem): Boolean {
            return oldItem == newItem
        }
    }
}
