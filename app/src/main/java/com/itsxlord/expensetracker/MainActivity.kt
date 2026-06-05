package com.itsxlord.expensetracker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itsxlord.expensetracker.data.AppDatabase
import com.itsxlord.expensetracker.data.ExpenseNote
import com.itsxlord.expensetracker.databinding.ActivityMainBinding
import com.itsxlord.expensetracker.databinding.DialogAddNoteBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NoteAdapter
    private val database by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Only handle side paddings to avoid system bar overlaps on curved screens
            v.updatePadding(left = systemBars.left, right = systemBars.right)
            
            // Handle the top system bar by letting AppBarLayout do its job or manually setting it once
            // We won't add it to the existing padding here to avoid doubling up
            insets
        }

        setupRecyclerView()
        setupListeners()
        observeNotes()
    }

    private fun setupRecyclerView() {
        adapter = NoteAdapter(
            onNoteClick = { noteWithItems ->
                val intent = Intent(this, NoteDetailsActivity::class.java).apply {
                    putExtra("NOTE_ID", noteWithItems.note.id)
                }
                startActivity(intent)
            },
            onNoteLongClick = { noteWithItems ->
                showDeleteNoteDialog(noteWithItems.note)
            }
        )
        binding.rvNotes.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.rvNotes.adapter = adapter
    }

    private fun setupListeners() {
        binding.fabAddNote.setOnClickListener {
            showAddNoteDialog()
        }
    }

    private fun showAddNoteDialog() {
        val dialogBinding = DialogAddNoteBinding.inflate(LayoutInflater.from(this))

        MaterialAlertDialogBuilder(this)
            .setTitle("New Note")
            .setView(dialogBinding.root)
            .setPositiveButton("Create") { _, _ ->
                val title = dialogBinding.etNoteTitle.text.toString()
                if (title.isNotBlank()) {
                    lifecycleScope.launch {
                        database.expenseDao().insertNote(
                            ExpenseNote(title = title)
                        )
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteNoteDialog(note: ExpenseNote) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Note?")
            .setMessage("Delete '${note.title}' and all expenses?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    database.expenseDao().deleteNote(note)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeNotes() {
        lifecycleScope.launch {
            database.expenseDao().getNotesWithItems().collect { notes ->
                adapter.submitList(notes)
            }
        }
    }
}
