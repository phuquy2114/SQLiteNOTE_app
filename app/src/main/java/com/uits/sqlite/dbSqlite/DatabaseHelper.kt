package com.uits.sqlite.dbSqlite

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.uits.sqlite.model.Note
import java.util.*

/**
 * DatabaseHelper
 *
 *
 * Copyright © 2019 UITS CO.,LTD
 * Created PHUQUY on 2019-12-14.
 */
class DatabaseHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    // Creating Tables
    override fun onCreate(db: SQLiteDatabase) {

        // create notes table
        db.execSQL(Note.CREATE_TABLE)
    }

    // Upgrading database
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + Note.TABLE_NAME)

        // Create tables again
        onCreate(db)
    }

    fun insertNote(note: String?): Long {
        // get writable database as we want to write data
        val db = this.writableDatabase
        val values = ContentValues()
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(Note.COLUMN_NOTE, note)

        // insert row
        val id = db.insert(Note.TABLE_NAME, null, values)

        // close db connection
        db.close()

        // return newly inserted row id
        return id
    }

    fun getNote(id: Long): Note {
        // get readable database as we are not inserting anything
        val db = this.readableDatabase
        val cursor = db.query(Note.TABLE_NAME, arrayOf(Note.COLUMN_ID, Note.COLUMN_NOTE, Note.COLUMN_TIMESTAMP),
                Note.COLUMN_ID + "=?", arrayOf(id.toString()), null, null, null, null)
        cursor?.moveToFirst()

        // prepare note object
        val note = Note(
                cursor!!.getInt(cursor.getColumnIndex(Note.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(Note.COLUMN_NOTE)),
                cursor.getString(cursor.getColumnIndex(Note.COLUMN_TIMESTAMP)))

        // close the db connection
        cursor.close()
        return note
    }

    // Select All Query
    val allNotes: List<Note>
        get() {
            val notes: MutableList<Note> = ArrayList()

            // Select All Query
            val selectQuery = "SELECT  * FROM " + Note.TABLE_NAME + " ORDER BY " +
                    Note.COLUMN_TIMESTAMP + " DESC"
            val db = this.writableDatabase
            val cursor = db.rawQuery(selectQuery, null)

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    val note = Note()
                    note.id = cursor.getInt(cursor.getColumnIndex(Note.COLUMN_ID))
                    note.note = cursor.getString(cursor.getColumnIndex(Note.COLUMN_NOTE))
                    note.timestamp = cursor.getString(cursor.getColumnIndex(Note.COLUMN_TIMESTAMP))
                    notes.add(note)
                } while (cursor.moveToNext())
            }

            // close db connection
            db.close()

            // return notes list
            return notes
        }

    // return count
    val notesCount: Int
        get() {
            val countQuery = "SELECT  * FROM " + Note.TABLE_NAME
            val db = this.readableDatabase
            val cursor = db.rawQuery(countQuery, null)
            val count = cursor.count
            cursor.close()


            // return count
            return count
        }

    fun updateNote(note: Note): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(Note.COLUMN_NOTE, note.note)

        // updating row
        return db.update(Note.TABLE_NAME, values, Note.COLUMN_ID + " = ?", arrayOf(note.id.toString()))
    }

    fun deleteNote(note: Note) {
        val db = this.writableDatabase
        db.delete(Note.TABLE_NAME, Note.COLUMN_ID + " = ?", arrayOf(note.id.toString()))
        db.close()
    }

    companion object {
        // Database Version
        private const val DATABASE_VERSION = 1

        // Database Name
        private const val DATABASE_NAME = "notes_db"
    }
}