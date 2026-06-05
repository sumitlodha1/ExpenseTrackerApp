package com.itsxlord.expensetracker

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itsxlord.expensetracker.data.AppDatabase
import com.itsxlord.expensetracker.data.ExpenseItem
import com.itsxlord.expensetracker.data.ExpenseNote
import com.itsxlord.expensetracker.databinding.ActivityNoteDetailsBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class NoteDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteDetailsBinding
    private lateinit var adapter: ExpenseAdapter
    private val database by lazy { AppDatabase.getDatabase(this) }
    private var noteId: Long = -1
    private var currentNote: ExpenseNote? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNoteDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            binding.appBar.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        noteId = intent.getLongExtra("NOTE_ID", -1)
        if (noteId == -1L) {
            finish()
            return
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
        setupListeners()
        observeNote()
        observeExpenses()
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter { item ->
            showDeleteItemDialog(item)
        }
        binding.rvExpenses.layoutManager = LinearLayoutManager(this)
        binding.rvExpenses.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnAddExpense.setOnClickListener {
            val amountStr = binding.etAmount.text.toString()
            val description = binding.etDescription.text.toString()

            if (amountStr.isNotBlank() && description.isNotBlank()) {
                val amount = amountStr.toDoubleOrNull()
                if (amount != null) {
                    saveExpense(amount, description)
                } else {
                    Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill both fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeNote() {
        lifecycleScope.launch {
            database.expenseDao().getNoteById(noteId).collect { note ->
                currentNote = note
                supportActionBar?.title = note.title
                updateTheme()
            }
        }
    }

    private fun updateTheme() {
        val bgColor = Color.parseColor("#edf6f9")
        binding.root.setBackgroundColor(bgColor)
        
        val contrastColor = Color.BLACK
        
        adapter.setTextColor(contrastColor)
        binding.toolbar.setTitleTextColor(contrastColor)
        binding.toolbar.navigationIcon?.setTint(contrastColor)
        
        window.statusBarColor = bgColor
        val wic = ViewCompat.getWindowInsetsController(window.decorView)
        wic?.isAppearanceLightStatusBars = true

        binding.etAmount.setTextColor(contrastColor)
        binding.etDescription.setTextColor(contrastColor)
        
        binding.btnAddExpense.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#006d77"))
        binding.btnAddExpense.setTextColor(Color.WHITE)
    }

    private fun saveExpense(amount: Double, description: String) {
        lifecycleScope.launch {
            val item = ExpenseItem(noteId = noteId, amount = amount, description = description)
            database.expenseDao().insertItem(item)
            binding.etAmount.text?.clear()
            binding.etDescription.text?.clear()
            binding.etAmount.clearFocus()
            binding.etDescription.clearFocus()
        }
    }

    private fun observeExpenses() {
        lifecycleScope.launch {
            database.expenseDao().getItemsForNote(noteId).collect { items ->
                adapter.submitList(items)
                updateTotal(items)
            }
        }
    }

    private fun updateTotal(items: List<ExpenseItem>) {
        val total = items.sumOf { it.amount }
        val currencyFormat = NumberFormat.getCurrencyInstance()
        binding.tvTotalAmount.text = currencyFormat.format(total)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteItemDialog(item: ExpenseItem) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Expense?")
            .setMessage("Remove this record?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    database.expenseDao().deleteItem(item)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
