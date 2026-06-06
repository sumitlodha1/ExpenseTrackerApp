package com.itsxlord.expensetracker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NoteAdapter
    private val database by lazy { AppDatabase.getDatabase(this) }
    private var currentView = ViewType.ACTIVE

    enum class ViewType { ACTIVE, ARCHIVED, DELETED }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.drawerLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(left = systemBars.left, right = systemBars.right)
            binding.mainContent.updatePadding(top = systemBars.top)
            insets
        }

        setupToolbar()
        setupDrawer()
        setupRecyclerView()
        setupListeners()
        observeNotes()
        cleanupDeletedNotes()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupDrawer() {
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_notes -> {
                    currentView = ViewType.ACTIVE
                    binding.toolbar.title = "My Expenses"
                    binding.fabAddNote.show()
                }
                R.id.nav_archive -> {
                    currentView = ViewType.ACTIVE // Reusing logic for now, filtering handled in observeNotes
                    currentView = ViewType.ARCHIVED
                    binding.toolbar.title = "Archive"
                    binding.fabAddNote.hide()
                }
                R.id.nav_deleted -> {
                    currentView = ViewType.DELETED
                    binding.toolbar.title = "Recently Deleted"
                    binding.fabAddNote.hide()
                }
            }
            observeNotes()
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupRecyclerView() {
        adapter = NoteAdapter(
            onNoteClick = { noteWithItems ->
                if (currentView == ViewType.DELETED) {
                    showRestoreDialog(noteWithItems.note)
                } else {
                    val intent = Intent(this, NoteDetailsActivity::class.java).apply {
                        putExtra("NOTE_ID", noteWithItems.note.id)
                    }
                    startActivity(intent)
                }
            },
            onNoteLongClick = { noteWithItems ->
                when (currentView) {
                    ViewType.ACTIVE -> showActiveNoteOptions(noteWithItems.note)
                    ViewType.ARCHIVED -> showArchivedNoteOptions(noteWithItems.note)
                    ViewType.DELETED -> showDeletePermanentlyDialog(noteWithItems.note)
                }
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

    private fun observeNotes() {
        lifecycleScope.launch {
            val flow = when (currentView) {
                ViewType.ACTIVE -> database.expenseDao().getActiveNotesWithItems()
                ViewType.ARCHIVED -> database.expenseDao().getArchivedNotesWithItems()
                ViewType.DELETED -> database.expenseDao().getDeletedNotesWithItems()
            }
            flow.collectLatest { notes ->
                adapter.submitList(notes)
            }
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
                        database.expenseDao().insertNote(ExpenseNote(title = title))
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showActiveNoteOptions(note: ExpenseNote) {
        val options = arrayOf("Archive", "Delete")
        MaterialAlertDialogBuilder(this)
            .setTitle(note.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> lifecycleScope.launch { database.expenseDao().updateNote(note.copy(isArchived = true)) }
                    1 -> lifecycleScope.launch { database.expenseDao().updateNote(note.copy(isDeleted = true, deletedAt = System.currentTimeMillis())) }
                }
            }
            .show()
    }

    private fun showArchivedNoteOptions(note: ExpenseNote) {
        val options = arrayOf("Unarchive", "Delete")
        MaterialAlertDialogBuilder(this)
            .setTitle(note.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> lifecycleScope.launch { database.expenseDao().updateNote(note.copy(isArchived = false)) }
                    1 -> lifecycleScope.launch { database.expenseDao().updateNote(note.copy(isDeleted = true, deletedAt = System.currentTimeMillis())) }
                }
            }
            .show()
    }

    private fun showRestoreDialog(note: ExpenseNote) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Restore Note?")
            .setMessage("Do you want to restore '${note.title}'?")
            .setPositiveButton("Restore") { _, _ ->
                lifecycleScope.launch { database.expenseDao().updateNote(note.copy(isDeleted = false, deletedAt = null)) }
            }
            .setNegativeButton("Delete Permanently") { _, _ ->
                showDeletePermanentlyDialog(note)
            }
            .show()
    }

    private fun showDeletePermanentlyDialog(note: ExpenseNote) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Permanently?")
            .setMessage("This will permanently delete '${note.title}' and all its expenses.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch { database.expenseDao().deleteNotePermanently(note) }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun cleanupDeletedNotes() {
        lifecycleScope.launch {
            val thirtyDaysMillis = 30L * 24 * 60 * 60 * 1000
            val threshold = System.currentTimeMillis() - thirtyDaysMillis
            database.expenseDao().deleteOldNotes(threshold)
        }
    }
}
