package com.example.todoapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class AddTask : AppCompatActivity() {
    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var timeNotificationInput: ImageView
    private lateinit var dateNotificationInput: ImageView
    private lateinit var notificationsButton: TextView
    private lateinit var addTaskButton: TextView
    private lateinit var notificationDateField: TextView
    private lateinit var notificationTimeField: TextView
    private lateinit var addAttachmentButton: TextView
    private lateinit var attachmentField: LinearLayout

    private var notificationTime : String = ""
    private var notificationDate : String = ""

    private lateinit var categorySpinner: Spinner
    private var category : String = "Wybierz kategorię..."

    private lateinit var dbManager: DatabaseManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.todo_task_creator)

        dbManager = DatabaseManager(DatabaseHelper(this))

        savedInstanceState?.let {
            category = it.getString("category", "Wybierz kategorię...")
            notificationDate = it.getString("notificationDate", "")
            notificationTime = it.getString("notificationTime", "")
        }

        setup()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("category", category)
        outState.putString("title", titleInput.text.toString())
        outState.putString("description", descriptionInput.text.toString())
        outState.putString("notificationDate", notificationDate)
        outState.putString("notificationTime", notificationTime)
    }

    private fun setup() {
        titleInput = findViewById(R.id.addTitleInput)
        descriptionInput = findViewById(R.id.addDescriptionInput)
        dateNotificationInput = findViewById(R.id.notificationCalendar)
        timeNotificationInput = findViewById(R.id.notificationClock)
        notificationsButton = findViewById(R.id.notificationButton)
        addTaskButton = findViewById(R.id.addTaskButton)
        notificationDateField = findViewById(R.id.addNotificationDateInput)
        notificationTimeField = findViewById(R.id.addNotificationTimeInput)
        addAttachmentButton = findViewById(R.id.addAttachmentButton)
        attachmentField = findViewById(R.id.attachmentField)
        categorySpinner = findViewById(R.id.selectCategoryInput)
        setSpinner()

        if(!notificationTime.isBlank()) notificationTimeField.text = notificationTime
        if(!notificationDate.isBlank()) notificationDateField.text = notificationDate

        addTaskButton.setOnClickListener {
            val title: String = titleInput.text.toString()
            val description: String = descriptionInput.text.toString()
            val now = LocalDateTime.now()
            val dateFormatter = DateTimeFormatter.ofPattern("d/M/yyyy")
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

            val currentDate = now.format(dateFormatter)
            val currentTime = now.format(timeFormatter)

            if (!validateText(title, "Nie wpisano tytułu!") ||
                !validateText(description, "Nie wpisano opisu!") ||
                !validateDate()
            ) {
                return@setOnClickListener
            }

            val newTask = Task(
                title = title,
                description = description,
                createDate = currentDate,
                createTime = currentTime,
                endDate = null,
                endTime = null,
                category = "Ogólna",
                notificationDate = if (notificationDate.isNotEmpty()) notificationDate else null,
                notificationTime = if (notificationTime.isNotEmpty()) notificationTime else null,
                hasAttachments = false,
                isDone = false
            )

            val result = dbManager.insertTask(newTask)

            if (result > -1) {
                Toast.makeText(this, "Zadanie dodane pomyślnie!", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Błąd podczas dodawania zadania.", Toast.LENGTH_LONG).show()
            }

        }

        dateNotificationInput.setOnClickListener { showDatePickerDialog() }
        timeNotificationInput.setOnClickListener { showTimePickerDialog() }
    }

    private fun validateText(text: String, message: String): Boolean {
        if (text.isBlank()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun validateDate(): Boolean {
        try {
            val formatter = DateTimeFormatter.ofPattern("d/M/yyyy HH:mm")
            val now = LocalDateTime.now()

            if (notificationDate.isNotEmpty()) {
                if (notificationTime.isEmpty()) {
                    Toast.makeText(this, "Nie wybrano godziny powiadomienia!", Toast.LENGTH_SHORT).show()
                    return false
                }

                val notificationDateTime = LocalDateTime.parse("$notificationDate $notificationTime", formatter)

                if (notificationDateTime.isBefore(now)) {
                    Toast.makeText(this, "Powiadomienie nie może być w przeszłości!", Toast.LENGTH_SHORT).show()
                    return false
                }
            } else {
                if (notificationTime.isNotEmpty()) {
                    Toast.makeText(this, "Nie wybrano daty powiadomienia!", Toast.LENGTH_SHORT).show()
                    return false
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Błąd formatu daty/godziny", Toast.LENGTH_SHORT).show()
            Log.e("validateDate", "Błąd parsowania daty: ${e.message}")
            return false
        }

        return true
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val d = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                notificationDate = d
                notificationDateField.text = notificationDate
            },
            year, month, day
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
        datePickerDialog.show()
    }

    @SuppressLint("DefaultLocale")
    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val t = String.format("%02d:%02d", selectedHour, selectedMinute)
                    notificationTime = t
                    notificationTimeField.text = notificationTime
            },
            hour, minute, true
        )

        timePickerDialog.show()
    }

    private fun setSpinner(){
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.category_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        val spinnerPosition = adapter.getPosition(category)

        categorySpinner.setSelection(spinnerPosition)

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCategory = parent.getItemAtPosition(position).toString()
                category = selectedCategory
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}

