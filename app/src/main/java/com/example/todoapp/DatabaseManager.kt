package com.example.todoapp

import android.content.ContentValues
import android.database.Cursor
import android.util.Log

class DatabaseManager(private val dbHelper: DatabaseHelper) {
    fun insertTask(task: Task): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_TITLE, task.title)
            put(DatabaseHelper.COLUMN_DESCRIPTION, task.description)
            put(DatabaseHelper.COLUMN_CREATE_DATE, task.createDate)
            put(DatabaseHelper.COLUMN_CREATE_TIME, task.createTime)
            put(DatabaseHelper.COLUMN_END_DATE, task.endDate)
            put(DatabaseHelper.COLUMN_END_TIME, task.endTime)
            put(DatabaseHelper.COLUMN_CATEGORY, task.category)
            put(DatabaseHelper.COLUMN_NOTIFICATION_DATE, task.notificationDate)
            put(DatabaseHelper.COLUMN_NOTIFICATION_TIME, task.notificationTime)
            put(DatabaseHelper.COLUMN_HAS_ATTACHMENTS, if (task.hasAttachments) 1 else 0)
        }
        return db.insert(DatabaseHelper.TABLE_TASKS, null, values)
    }

    private fun parseCursorToTask(cursor: Cursor): Task {
        return Task(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE)),
            description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION)),
            createDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATE_DATE)),
            createTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATE_TIME)),
            endDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_END_DATE)),
            endTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_END_TIME)),
            category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY)),
            notificationDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIFICATION_DATE)),
            notificationTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIFICATION_TIME)),
            hasAttachments = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HAS_ATTACHMENTS)) == 1,
            isDone = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_DONE)) == 1
        )
    }

    private fun queryTasks(
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        orderBy: String? = null
    ): List<Task> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_TASKS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            orderBy
        )

        val tasks = mutableListOf<Task>()
        with(cursor) {
            while (moveToNext()) {
                tasks.add(parseCursorToTask(this))
            }
        }
        cursor.close()
        return tasks
    }

    fun getTaskById(id : Int) : Task {
        val task = queryTasks("${DatabaseHelper.COLUMN_ID} = ?", arrayOf(id.toString()))
        return task.first()
    }

    fun getTasks(sequence: String? = null, category: String? = null, sorted: Boolean = false): List<Task> {
        val selectionParts = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()

        if (!sequence.isNullOrEmpty()) {
            selectionParts.add("${DatabaseHelper.COLUMN_TITLE} LIKE ?")
            selectionArgs.add("%$sequence%")
        }

        if (!category.isNullOrEmpty()) {
            selectionParts.add("${DatabaseHelper.COLUMN_CATEGORY} = ?")
            selectionArgs.add(category)
        }

        val selection = if (selectionParts.isNotEmpty()) selectionParts.joinToString(" AND ") else null
        val orderBy = if (sorted) "${DatabaseHelper.COLUMN_CREATE_DATE} ASC, ${DatabaseHelper.COLUMN_CREATE_TIME} ASC" else null

        Log.d("XD", "XD")

        return queryTasks(selection, if (selectionArgs.isNotEmpty()) selectionArgs.toTypedArray() else null, orderBy)
    }

    fun updateTask(task: Task): Int {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_TITLE, task.title)
            put(DatabaseHelper.COLUMN_DESCRIPTION, task.description)
            put(DatabaseHelper.COLUMN_CREATE_DATE, task.createDate)
            put(DatabaseHelper.COLUMN_CREATE_TIME, task.createTime)
            put(DatabaseHelper.COLUMN_END_DATE, task.endDate)
            put(DatabaseHelper.COLUMN_END_TIME, task.endTime)
            put(DatabaseHelper.COLUMN_CATEGORY, task.category)
            put(DatabaseHelper.COLUMN_NOTIFICATION_DATE, task.notificationDate)
            put(DatabaseHelper.COLUMN_NOTIFICATION_TIME, task.notificationTime)
            put(DatabaseHelper.COLUMN_HAS_ATTACHMENTS, if (task.hasAttachments) 1 else 0)
            put(DatabaseHelper.COLUMN_IS_DONE, if (task.isDone) 1 else 0)
        }

        val selection = "${DatabaseHelper.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(task.id.toString())

        return db.update(
            DatabaseHelper.TABLE_TASKS,
            values,
            selection,
            selectionArgs
        )
    }
}