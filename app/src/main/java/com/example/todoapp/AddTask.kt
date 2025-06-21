package com.example.todoapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class AddTask : AppCompatActivity() {
    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var dateInput: ImageView
    private lateinit var timeInput: ImageView
    private lateinit var timeNotificationInput: ImageView
    private lateinit var dateNotificationInput: ImageView
    private lateinit var notificationsButton: TextView
    private lateinit var addTaskButton: TextView
    private lateinit var dateField: TextView
    private lateinit var timeField: TextView
    private lateinit var notificationDateField: TextView
    private lateinit var notificationTimeField: TextView

    private var date : String = ""
    private var time : String = ""
    private var notificationTime : String = ""
    private var notificationDate : String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.todo_task_creator)

        savedInstanceState?.let {
            date = it.getString("date", "")
            time = it.getString("time", "")
            notificationDate = it.getString("notificationDate", "")
            notificationTime = it.getString("notificationTime", "")
        }

        setup()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("title", titleInput.text.toString())
        outState.putString("description", descriptionInput.text.toString())
        outState.putString("date", date)
        outState.putString("time", time)
        outState.putString("notificationDate", notificationDate)
        outState.putString("notificationTime", notificationTime)
    }

    private fun setup() {
        titleInput = findViewById(R.id.addTitleInput)
        descriptionInput = findViewById(R.id.addDescriptionInput)
        dateInput = findViewById(R.id.calendar)
        timeInput = findViewById(R.id.clock)
        dateNotificationInput = findViewById(R.id.notificationCalendar)
        timeNotificationInput = findViewById(R.id.notificationClock)
        notificationsButton = findViewById(R.id.notificationButton)
        addTaskButton = findViewById(R.id.addTaskButton)
        dateField = findViewById(R.id.addDateInput)
        timeField = findViewById(R.id.addTimeInput)
        notificationDateField = findViewById(R.id.addNotificationDateInput)
        notificationTimeField = findViewById(R.id.addNotificationTimeInput)

        if(date != "") dateField.text = date
        if(time != "") timeField.text = time
        if(notificationTime != "") notificationTimeField.text = notificationTime
        if(notificationDate != "") notificationDateField.text = notificationDate

        addTaskButton.setOnClickListener {
            val title : String = titleInput.text.toString()
            val description : String = descriptionInput.text.toString()

            if(!validateText(title, "Nie wpisano tytułu!") ||
                !validateText(description, "Nie wpisano opisu!") ||
                !validateDate()){
                return@setOnClickListener
            }
            Log.d("InputData", "$title $description $date $time $notificationDate $notificationTime")
        }

        dateInput.setOnClickListener { showDatePickerDialog(false) }
        dateNotificationInput.setOnClickListener { showDatePickerDialog(true) }
        timeInput.setOnClickListener { showTimePickerDialog(false) }
        timeNotificationInput.setOnClickListener { showTimePickerDialog(true) }
    }

    private fun validateText(text: String, message: String): Boolean {
        if (text == "") {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun validateDate(): Boolean {
        if (date == "") {
            Toast.makeText(this, "Nie wybrałeś daty!", Toast.LENGTH_SHORT).show()
            return false
        }
        if(time == ""){
            Toast.makeText(this, "Nie wybrałeś godziny!", Toast.LENGTH_SHORT).show()
            return false
        }
        try {
            val formatter = DateTimeFormatter.ofPattern("d/M/yyyy H:mm")
            val selectedDateTime = LocalDateTime.parse("$date $time", formatter)
            val now = LocalDateTime.now()

            if (selectedDateTime.isBefore(now)) {
                Toast.makeText(this, "Data musi być z przyszłości!", Toast.LENGTH_SHORT).show()
                return false
            }

            if (notificationDate.isNotEmpty()) {
                if (notificationTime.isEmpty()) {
                    Toast.makeText(this, "Nie wybrano godziny powiadomienia!", Toast.LENGTH_SHORT).show()
                    return false
                }

                val notificationDateTime =
                    LocalDateTime.parse("$notificationDate $notificationTime", formatter)

                if (notificationDateTime.isBefore(now)) {
                    Toast.makeText(this, "Powiadomienie nie może być w przeszłości!", Toast.LENGTH_SHORT).show()
                    return false
                }

                if (notificationDateTime.isAfter(selectedDateTime)) {
                    Toast.makeText(this, "Powiadomienie nie może być później niż zaplanowana data!", Toast.LENGTH_SHORT).show()
                    return false
                }
            }
            else{
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

    private fun showDatePickerDialog(isNotification: Boolean) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val d = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                if(isNotification){
                    notificationDate = d
                    notificationDateField.text = notificationDate
                }
                else{
                    date = d
                    dateField.text = d
                }
            },
            year, month, day
        )

        datePickerDialog.show()
    }

    @SuppressLint("DefaultLocale")
    private fun showTimePickerDialog(isNotification : Boolean) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val t = String.format("%02d:%02d", selectedHour, selectedMinute)
                if(isNotification) {
                    notificationTime = t
                    notificationTimeField.text = notificationTime
                }
                else {
                    time = t
                    timeField.text = t
                }
            },
            hour, minute, true
        )

        timePickerDialog.show()
    }
}

