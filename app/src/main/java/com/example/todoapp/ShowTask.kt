package com.example.todoapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
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
    private lateinit var toggleNotificationButton: ImageView
    private lateinit var attachmentsField: LinearLayout

    private lateinit var task: Task

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.task)

        dbManager = DatabaseManager(DatabaseHelper(this))

        val prefs = getSharedPreferences("task_prefs", MODE_PRIVATE)
        val taskId = prefs.getInt("selected_task_id", -1)

        if (taskId != -1) {
            task = dbManager.getTaskById(taskId)
        }

        setup()
    }

    @SuppressLint("SetTextI18n")
    private fun setup() {
        titleField = findViewById(R.id.taskTitle)
        titleField.text = task.title

        categoryField = findViewById(R.id.taskCategory)
        categoryField.text = "Kategoria: ${task.category}"

        descriptionField = findViewById(R.id.taskDescription)
        descriptionField.text = task.description

        planedDateField = findViewById(R.id.taskPlanedDate)
        planedDateField.text = "Zaplanowano na: ${task.planedDate} ${task.planedTime}"

        notificationDateField = findViewById(R.id.taskNotificationDate)
        if (task.notificationDate != null && task.notificationTime != null) {
            notificationDateField.text = "Powiadomienie: ${task.notificationDate} ${task.notificationTime}"
            notificationDateField.visibility = VISIBLE
        }

        createdDateField = findViewById(R.id.taskCreatedDate)
        createdDateField.text = "Utworzono ${task.createDate} ${task.createTime}"

        endDateField = findViewById(R.id.taskEndDate)
        endButton = findViewById(R.id.endTaskButton)
        if (task.isDone) {
            endDateField.text = "Zakończono: ${task.endDate} ${task.endTime}"
            endDateField.visibility = VISIBLE
            endButton.visibility = GONE
        }

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
            setActiveTaskIndex(-1)
            finish()
        }

        deleteButton = findViewById(R.id.taskDelete)
        deleteButton.setOnClickListener {
            //TODO usuwanie
            setActiveTaskIndex(-1)
            finish()
        }

        editButton = findViewById(R.id.taskEdit)
        toggleNotificationButton = findViewById(R.id.taskNotifications)
        attachmentsField = findViewById(R.id.attachmentField)
    }

    private fun setActiveTaskIndex(index : Int){
        val sharedPref = getSharedPreferences("task_prefs", MODE_PRIVATE)
        sharedPref.edit { putInt("selected_task_id", index) }
    }
}