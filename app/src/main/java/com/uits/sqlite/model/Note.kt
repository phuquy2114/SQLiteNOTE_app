package com.uits.sqlite.model

/**
 * Note
 * Copyright © 2019 UITS CO.,LTD
 * Created PHUQUY on 2019-12-14.
 */
class Note {
    var id = 0
    var note: String? = null
    var timestamp: String? = null

    companion object {
        const val TABLE_NAME = "notes"
        const val COLUMN_ID = "id"
        const val COLUMN_NOTE = "note"
        const val COLUMN_TIMESTAMP = "timestamp"

        // Create table SQL query
        const val CREATE_TABLE = ("CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NOTE + " TEXT,"
                + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")")
    }

    constructor() {}
    constructor(id: Int, note: String?, timestamp: String?) {
        this.id = id
        this.note = note
        this.timestamp = timestamp
    }


}