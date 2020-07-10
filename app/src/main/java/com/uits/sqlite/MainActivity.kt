package com.uits.sqlite

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.uits.sqlite.dbSqlite.DatabaseHelper
import com.uits.sqlite.model.Note
import com.uits.sqlite.utils.MyDividerItemDecoration
import com.uits.sqlite.utils.RecyclerTouchListener
import com.uits.sqlite.utils.RecyclerTouchListener.ClickListener
import java.util.*

class MainActivity : AppCompatActivity() {

    private var mAdapter: NotesAdapter? = null
    private val notesList: MutableList<Note> = ArrayList()
    private var coordinatorLayout: CoordinatorLayout? = null
    private lateinit var recyclerView: RecyclerView
    private var noNotesView: TextView? = null
    private var db: DatabaseHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        // init view

        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        coordinatorLayout = findViewById(R.id.coordinator_layout)
        recyclerView = findViewById(R.id.recycler_view)
        noNotesView = findViewById(R.id.empty_notes_view)

        // initialization database
        db = DatabaseHelper(this)
        notesList.addAll(db!!.allNotes)


        fab.setOnClickListener { showNoteDialog(false, null, -1) }

        // init adapter
        mAdapter = NotesAdapter(this, notesList)
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(applicationContext)
        recyclerView.setLayoutManager(mLayoutManager)
        recyclerView.setItemAnimator(DefaultItemAnimator())
        recyclerView.addItemDecoration(MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16))
        recyclerView.setAdapter(mAdapter)

        toggleEmptyNotes()

        /**
         * On long press on RecyclerView item, open alert dialog
         * with options to choose
         * Edit and Delete
         */
        recyclerView.addOnItemTouchListener(RecyclerTouchListener(this,
                recyclerView, object : ClickListener {

            override fun onClick(view: View?, position: Int) {

            }

            override fun onLongClick(view: View?, position: Int) {
                showActionsDialog(position)
            }

        }))
    }

    /**
     * Inserting new note in db
     * and refreshing the list
     */
    private fun createNote(note: String) {
        // inserting note in db and getting
        // newly inserted note id
        val id = db!!.insertNote(note)

        // get the newly inserted note from db
        val n = db!!.getNote(id)
        if (n != null) {
            // adding new note to array list at 0 position
            notesList.add(0, n)

            // refreshing the list
            mAdapter!!.notifyDataSetChanged()
            toggleEmptyNotes()
        }
    }

    /**
     * Updating note in db and updating
     * item in the list by its position
     */
    private fun updateNote(note: String, position: Int) {
        val n = notesList[position]
        // updating note text
        n.note = note

        // updating note in db
        db!!.updateNote(n)

        // refreshing the list
        notesList[position] = n
        mAdapter!!.notifyItemChanged(position)
        toggleEmptyNotes()
    }

    /**
     * Deleting note from SQLite and removing the
     * item from the list by its position
     */
    private fun deleteNote(position: Int) {
        // deleting the note from db
        db!!.deleteNote(notesList[position])

        // removing the note from the list
        notesList.removeAt(position)
        mAdapter!!.notifyItemRemoved(position)
        toggleEmptyNotes()
    }

    /**
     * Opens dialog with Edit - Delete options
     * Edit - 0
     * Delete - 0
     */
    private fun showActionsDialog(position: Int) {
        val colors = arrayOf<CharSequence>("Edit", "Delete")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose option")
        builder.setItems(colors) { dialog, which ->
            if (which == 0) {
                showNoteDialog(true, notesList[position], position)
            } else {
                deleteNote(position)
            }
        }
        builder.show()
    }

    /**
     * Shows alert dialog with EditText options to enter / edit
     * a note.
     * when shouldUpdate=true, it automatically displays old note and changes the
     * button text to UPDATE
     */
    private fun showNoteDialog(shouldUpdate: Boolean, note: Note?, position: Int) {
        val layoutInflaterAndroid = LayoutInflater.from(applicationContext)
        val view = layoutInflaterAndroid.inflate(R.layout.note_dialog, null)
        val alertDialogBuilderUserInput = AlertDialog.Builder(this@MainActivity)
        alertDialogBuilderUserInput.setView(view)
        val inputNote = view.findViewById<EditText>(R.id.note)
        val dialogTitle = view.findViewById<TextView>(R.id.dialog_title)
        dialogTitle.text = if (!shouldUpdate) getString(R.string.lbl_new_note_title) else getString(R.string.lbl_edit_note_title)
        if (shouldUpdate && note != null) {
            inputNote.setText(note.note)
        }
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(if (shouldUpdate) "update" else "save") { dialogBox, id -> }
                .setNegativeButton("cancel"
                ) { dialogBox, id -> dialogBox.cancel() }
        val alertDialog = alertDialogBuilderUserInput.create()
        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(View.OnClickListener {
            // Show toast message when no text is entered
            if (TextUtils.isEmpty(inputNote.text.toString())) {
                Toast.makeText(this@MainActivity, "Enter note!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            } else {
                alertDialog.dismiss()
            }

            // check if user updating note
            if (shouldUpdate && note != null) {
                // update note by it's id
                updateNote(inputNote.text.toString(), position)
            } else {
                // create new note
                createNote(inputNote.text.toString())
            }
        })
    }

    /**
     * Toggling list and empty notes view
     */
    private fun toggleEmptyNotes() {
        // you can check notesList.size() > 0
        if (db!!.notesCount > 0) {
            noNotesView!!.visibility = View.GONE
        } else {
            noNotesView!!.visibility = View.VISIBLE
        }
    }
}