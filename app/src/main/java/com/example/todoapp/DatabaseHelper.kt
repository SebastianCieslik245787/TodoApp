package com.example.todoapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "task_manager.db"
        private const val DATABASE_VERSION = 2

        const val TABLE_TASKS = "tasks"
        const val TABLE_ATTACHMENTS = "attachments"
        const val COLUMN_TASK_ID = "task_id"
        const val COLUMN_FORMAT = "format"
        const val COLUMN_FILENAME = "filename"
        const val COLUMN_LOCAL_PATH = "local_path"

        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_CREATE_DATE = "create_date"
        const val COLUMN_CREATE_TIME = "create_time"
        const val COLUMN_END_DATE = "end_date"
        const val COLUMN_END_TIME = "end_time"
        const val COLUMN_PLANED_DATE = "planed_date"
        const val COLUMN_PLANED_TIME = "planed_time"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_NOTIFICATION_DATE = "notification_date"
        const val COLUMN_NOTIFICATION_TIME = "notification_time"
        const val COLUMN_HAS_ATTACHMENTS = "has_attachments"
        const val COLUMN_IS_DONE = "is_done"
        const val COLUMN_NOTIFICATION_ON = "notification_on"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE $TABLE_TASKS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT NOT NULL,
                $COLUMN_CREATE_DATE TEXT NOT NULL,
                $COLUMN_CREATE_TIME TEXT NOT NULL,
                $COLUMN_END_DATE TEXT,
                $COLUMN_END_TIME TEXT,
                $COLUMN_PLANED_DATE TEXT NOT NULL,
                $COLUMN_PLANED_TIME TEXT NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_NOTIFICATION_DATE TEXT,
                $COLUMN_NOTIFICATION_TIME TEXT,
                $COLUMN_HAS_ATTACHMENTS INTEGER DEFAULT 0,
                $COLUMN_IS_DONE INTEGER DEFAULT 0,
                $COLUMN_NOTIFICATION_ON INTEGER DEFAULT 0
            )
        """.trimIndent()
        db?.execSQL(createTable)

        val createTableAttachments = """
            CREATE TABLE $TABLE_ATTACHMENTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TASK_ID INTEGER NOT NULL,
                $COLUMN_FORMAT TEXT NOT NULL,
                $COLUMN_FILENAME TEXT NOT NULL,
                $COLUMN_LOCAL_PATH TEXT NOT NULL
            )
        """.trimIndent()
        db?.execSQL(createTableAttachments)
    }

    override fun onUpgrade(
        db: SQLiteDatabase?,
        oldVersion: Int,
        newVersion: Int
    ) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ATTACHMENTS")
        onCreate(db)
    }
}
