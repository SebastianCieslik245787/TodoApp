package com.example.todoapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.edit
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ShowTask : AppCompatActivity() {
    private lateinit var dbManager: DatabaseManager

    private lateinit var titleField: TextView
    private lateinit var descriptionField: TextView
    private lateinit var planedDateField: TextView
    private lateinit var categoryField: TextView
    private lateinit var notificationDateField: TextView
    private lateinit var createdDateField: TextView
    private lateinit var endDateField: TextView
    private lateinit var endButton: TextView
    private lateinit var backButton: ImageView
    private lateinit var deleteButton: ImageView
    private lateinit var editButton: ImageView
    private lateinit var attachmentsField: LinearLayout

    private lateinit var task: Task
    private var attachments: List<Attachment> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.task)

        dbManager = DatabaseManager(DatabaseHelper(this))

        val prefs = getSharedPreferences("task_prefs", MODE_PRIVATE)
        val taskId = prefs.getInt("selected_task_id", -1)

        if (taskId != -1) {
            task = dbManager.getTaskById(taskId)
            attachments = dbManager.getAttachmentsForTask(task.id)
            Log.d("TaskID", "${task.id} ${attachments.size}")
        }

        setup()
        fillFields()
    }

    override fun onResume() {
        super.onResume()
        task = dbManager.getTaskById(task.id)
        attachments = dbManager.getAttachmentsForTask(task.id)

        fillFields()
    }

    @SuppressLint("SetTextI18n")
    private fun setup() {
        titleField = findViewById(R.id.taskTitle)
        categoryField = findViewById(R.id.taskCategory)
        descriptionField = findViewById(R.id.taskDescription)
        planedDateField = findViewById(R.id.taskPlanedDate)
        notificationDateField = findViewById(R.id.taskNotificationDate)
        createdDateField = findViewById(R.id.taskCreatedDate)
        endDateField = findViewById(R.id.taskEndDate)
        endButton = findViewById(R.id.endTaskButton)

        endButton.setOnClickListener {
            val now = LocalDateTime.now()
            val dateFormatter = DateTimeFormatter.ofPattern("d/M/yyyy")
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

            val currentDate = now.format(dateFormatter)
            val currentTime = now.format(timeFormatter)

            task.endTime = currentTime
            task.endDate = currentDate
            task.isDone = true

            dbManager.updateTask(task)

            endDateField.text = "Zakończono: $currentDate $currentTime"
            endDateField.visibility = VISIBLE
            endButton.visibility = GONE
        }

        backButton = findViewById(R.id.taskBack)
        backButton.setOnClickListener {
            setActiveTaskIndex()
            finish()
        }

        deleteButton = findViewById(R.id.taskDelete)
        deleteButton.setOnClickListener {
            showDeleteTaskDialog()
        }

        editButton = findViewById(R.id.taskEdit)
        editButton.setOnClickListener {
            val intent = Intent(this, AddTask::class.java)
            startActivity(intent)
        }
        attachmentsField = findViewById(R.id.AttachmentsField)
    }

    private fun setActiveTaskIndex() {
        val sharedPref = getSharedPreferences("task_prefs", MODE_PRIVATE)
        sharedPref.edit { putInt("selected_task_id", -1) }
    }

    private fun showDeleteTaskDialog() {
        AlertDialog.Builder(this)
            .setTitle("Usunąć zadanie?")
            .setMessage("Czy na pewno chcesz usunąć zadanie?")
            .setPositiveButton("Usuń") { _, _ ->
                dbManager.deleteTaskById(task.id)
                setActiveTaskIndex()
                finish()
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    @SuppressLint("SetTextI18n")
    private fun fillFields(){
        titleField.text = task.title
        categoryField.text = "Kategoria: ${task.category}"
        descriptionField.text = task.description
        planedDateField.text = "Zaplanowano na: ${task.planedDate} ${task.planedTime}"
        if (task.notificationDate != null && task.notificationTime != null) {
            notificationDateField.text =
                "Powiadomienie: ${task.notificationDate} ${task.notificationTime}"
            notificationDateField.visibility = VISIBLE
        } else {
            notificationDateField.visibility = GONE
        }
        createdDateField.text = "Utworzono ${task.createDate} ${task.createTime}"
        if (task.isDone) {
            endDateField.text = "Zakończono: ${task.endDate} ${task.endTime}"
            endDateField.visibility = VISIBLE
            endButton.visibility = GONE
        } else {
            endDateField.visibility = GONE
            endButton.visibility = VISIBLE
        }

        attachmentsField.removeAllViews()

        for (attachment in attachments){
            Log.d("ShowTask", "Dodaję widok dla załącznika: ${attachment.fileName}")
            addAttachmentView(attachment)
        }
    }

    private fun addAttachmentView(attachment: Attachment) {
        val view = layoutInflater.inflate(R.layout.attachment, attachmentsField, false)

        val attachmentName: TextView = view.findViewById(R.id.attachmentName)
        val deleteAttachmentButton: ImageView = view.findViewById(R.id.deleteAttachmentIcon)
        deleteAttachmentButton.visibility = GONE
        attachmentName.text = attachment.fileName

        attachmentName.setOnClickListener {
            openFile(attachment.localPath, attachment.format)
        }

        attachmentsField.addView(view)
    }

    private fun openFile(filePath: String, mimeType: String) {
        val file = File(filePath)
        if (!file.exists()) {
            Toast.makeText(this, "Plik nie istnieje!", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivity(Intent.createChooser(intent, "Otwórz za pomocą..."))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "Brak aplikacji do otwarcia tego typu pliku!", Toast.LENGTH_SHORT).show()
        }
    }
}