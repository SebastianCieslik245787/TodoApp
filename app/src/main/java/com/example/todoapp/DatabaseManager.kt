package com.example.todoapp

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.File
import androidx.core.database.sqlite.transaction

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
            put(DatabaseHelper.COLUMN_PLANED_DATE, task.planedDate)
            put(DatabaseHelper.COLUMN_PLANED_TIME, task.planedTime)
            put(DatabaseHelper.COLUMN_CATEGORY, task.category)
            put(DatabaseHelper.COLUMN_NOTIFICATION_DATE, task.notificationDate)
            put(DatabaseHelper.COLUMN_NOTIFICATION_TIME, task.notificationTime)
            put(DatabaseHelper.COLUMN_HAS_ATTACHMENTS, if (task.hasAttachments) 1 else 0)
        }
        return db.insert(DatabaseHelper.TABLE_TASKS, null, values)
    }

    fun insertAttachment(attachment: Attachment): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_TASK_ID, attachment.taskId)
            put(DatabaseHelper.COLUMN_FORMAT, attachment.format)
            put(DatabaseHelper.COLUMN_FILENAME, attachment.fileName)
            put(DatabaseHelper.COLUMN_LOCAL_PATH, attachment.localPath)
        }
        return db.insert(DatabaseHelper.TABLE_ATTACHMENTS, null, values)
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
            planedDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLANED_DATE)),
            planedTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLANED_TIME)),
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

    fun getTasks(
        sequence: String? = null,
        category: String? = null,
        sorted: Boolean = false,
        showDone: Boolean = true
    ): List<Task> {
        val selectionParts = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()

        if (!sequence.isNullOrEmpty()) {
            selectionParts.add("${DatabaseHelper.COLUMN_TITLE} LIKE ?")
            selectionArgs.add("%$sequence%")
        }

        if (!category.isNullOrEmpty() && category != "Wybierz kategorię" && category != "Wybierz kategorię...") {
            selectionParts.add("${DatabaseHelper.COLUMN_CATEGORY} = ?")
            selectionArgs.add(category)
        }

        if (!showDone) {
            selectionParts.add("${DatabaseHelper.COLUMN_IS_DONE} = ?")
            selectionArgs.add("0")
        }

        val selection = if (selectionParts.isNotEmpty()) selectionParts.joinToString(" AND ") else null
        val orderBy = if (sorted) "${DatabaseHelper.COLUMN_PLANED_DATE} ASC, ${DatabaseHelper.COLUMN_PLANED_TIME} ASC" else null

        return queryTasks(
            selection = selection,
            selectionArgs = if (selectionArgs.isNotEmpty()) selectionArgs.toTypedArray() else null,
            orderBy = orderBy
        )
    }

    fun getAttachmentsForTask(taskId : Int) : List<Attachment>{
        val db = dbHelper.readableDatabase
        val attachments = mutableListOf<Attachment>()

        val cursor = db.query(
            DatabaseHelper.TABLE_ATTACHMENTS,
            null,
            "${DatabaseHelper.COLUMN_TASK_ID} = ?",
            arrayOf(taskId.toString()),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                val format = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FORMAT))
                val fileName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FILENAME))
                val localPath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCAL_PATH))

                val attachment = Attachment(
                    id = id,
                    taskId = taskId,
                    format = format,
                    fileName = fileName,
                    localPath =localPath
                )

                attachments.add(attachment)
            } while (cursor.moveToNext())
        }

        return attachments
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
            put(DatabaseHelper.COLUMN_PLANED_DATE, task.planedDate)
            put(DatabaseHelper.COLUMN_PLANED_TIME, task.planedTime)
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

    fun deleteTaskById(id: Int): Boolean {
        val db = dbHelper.writableDatabase
        var success = false
        try {
            db.transaction {
                deleteAttachmentsByTaskId(this, id)

                val result = delete(DatabaseHelper.TABLE_TASKS, "${DatabaseHelper.COLUMN_ID} = ?", arrayOf(id.toString()))
                if (result > 0) {
                    success = true
                }
            }
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Transakcja usuwania zadania $id nie powiodła się", e)
            success = false
        }
        return success
    }

    fun deleteAttachmentById(id: Int): Boolean {
        val db = dbHelper.writableDatabase
        val result = db.delete(DatabaseHelper.TABLE_ATTACHMENTS, "${DatabaseHelper.COLUMN_ID} = ?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }

    private fun deleteAttachmentsByTaskId(db: SQLiteDatabase, taskId: Int) {
        val attachmentsToDelete = getAttachmentsForTask(taskId)

        val deletedRows = db.delete(DatabaseHelper.TABLE_ATTACHMENTS, "${DatabaseHelper.COLUMN_TASK_ID} = ?", arrayOf(taskId.toString()))

        if (deletedRows > 0) {
            attachmentsToDelete.forEach { attachment ->
                try {
                    val file = File(attachment.localPath)
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: Exception) {
                    Log.e("DatabaseManager", "Nie udało się usunąć pliku: ${attachment.localPath}", e)
                }
            }
        }
    }
}