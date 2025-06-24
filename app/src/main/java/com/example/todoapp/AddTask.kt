package com.example.todoapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.properties.Delegates

class AddTask : AppCompatActivity() {
    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var timeNotificationInput: ImageView
    private lateinit var dateNotificationInput: ImageView
    private lateinit var planedTimeInput: ImageView
    private lateinit var planedDateInput: ImageView
    private lateinit var notificationsButton: TextView
    private lateinit var addTaskButton: TextView
    private lateinit var notificationDateField: TextView
    private lateinit var notificationTimeField: TextView
    private lateinit var notificationDateLabel: TextView
    private lateinit var notificationTimeLabel: TextView
    private lateinit var planedDateField: TextView
    private lateinit var planedTimeField: TextView
    private lateinit var addAttachmentButton: TextView
    private lateinit var attachmentField: LinearLayout
    private lateinit var backButton: ImageView
    private lateinit var notificationIcon: ImageView

    private val viewModel: AddTaskViewModel by viewModels()

    private lateinit var categorySpinner: Spinner
    private var category: String = "Wybierz kategorię"

    private lateinit var dbManager: DatabaseManager

    private lateinit var task: Task
    private var currentTaskId by Delegates.notNull<Int>()

    private val PICK_FILE_REQUEST_CODE = 1001

    private lateinit var notificationScheduler: NotificationScheduler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.todo_task_creator)

        dbManager = DatabaseManager(DatabaseHelper(this))

        val prefs = getSharedPreferences("task_prefs", MODE_PRIVATE)
        currentTaskId = prefs.getInt("selected_task_id", -1)

        notificationScheduler = NotificationScheduler(applicationContext)

        if (savedInstanceState == null && currentTaskId != -1) {
            val task = dbManager.getTaskById(currentTaskId)
            val attachments = dbManager.getAttachmentsForTask(task.id)
            fillViewModelFromTask(task, attachments)
        }

        setup()
        setVisibilityNotification()
        for (i in viewModel.attachmentsList) addAttachmentView(i)
    }

    private fun fillViewModelFromTask(task: Task, attachments: List<Attachment>) {
        viewModel.title = task.title
        viewModel.description = task.description
        viewModel.planedDate = task.planedDate
        viewModel.planedTime = task.planedTime
        if (task.notificationDate != null && task.notificationTime != null) {
            viewModel.notificationDate = task.notificationDate.toString()
            viewModel.notificationTime = task.notificationTime.toString()
            viewModel.notificationOn = true
        }
        viewModel.category = task.category
        viewModel.hasAttachment = task.hasAttachments
        viewModel.attachmentsList.clear()
        viewModel.attachmentsList.addAll(attachments)
    }

    @SuppressLint("ScheduleExactAlarm", "SetTextI18n")
    private fun setup() {
        titleInput = findViewById(R.id.addTitleInput)
        descriptionInput = findViewById(R.id.addDescriptionInput)
        dateNotificationInput = findViewById(R.id.notificationCalendar)
        timeNotificationInput = findViewById(R.id.notificationClock)
        planedDateInput = findViewById(R.id.planedCalendar)
        planedTimeInput = findViewById(R.id.planedClock)
        notificationsButton = findViewById(R.id.notificationButton)
        addTaskButton = findViewById(R.id.addTaskButton)
        notificationDateField = findViewById(R.id.addNotificationDateInput)
        notificationTimeField = findViewById(R.id.addNotificationTimeInput)
        planedDateField = findViewById(R.id.addPlanedDateInput)
        planedTimeField = findViewById(R.id.addPlanedTimeInput)
        addAttachmentButton = findViewById(R.id.addAttachmentButton)
        attachmentField = findViewById(R.id.attachmentField)
        categorySpinner = findViewById(R.id.selectCategoryInput)
        setSpinner()
        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener { showDiscardChangesDialog() }
        notificationDateLabel = findViewById(R.id.addNotificationDateLabel)
        notificationTimeLabel = findViewById(R.id.addNotificationTimeLabel)
        notificationIcon = findViewById(R.id.notificationButtonIcon)

        notificationTimeField.text = viewModel.notificationTime
        notificationDateField.text = viewModel.notificationDate
        planedTimeField.text = viewModel.planedTime
        planedDateField.text = viewModel.planedDate
        titleInput.setText(viewModel.title)
        descriptionInput.setText(viewModel.description)

        if (currentTaskId != -1) addTaskButton.text = "Zapisz"


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
                !validateDate() ||
                !validateCategory()
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
                planedDate = viewModel.planedDate,
                planedTime = viewModel.planedTime,
                category = category,
                notificationDate = if (viewModel.notificationOn && viewModel.notificationDate.isNotEmpty() && viewModel.notificationDate != "Wybierz datę") viewModel.notificationDate else null,
                notificationTime = if (viewModel.notificationOn && viewModel.notificationTime.isNotEmpty() && viewModel.notificationTime != "Wybierz godzinę") viewModel.notificationTime else null,
                hasAttachments = viewModel.hasAttachment,
                isDone = false,
                notificationOn = viewModel.notificationOn
            )

            if (currentTaskId != -1) {
                newTask.id = currentTaskId
                val result = dbManager.updateTask(newTask)
                if (result > -1) {
                    for (i in 0 until viewModel.attachmentsList.size) {
                        if (viewModel.attachmentsList[i].taskId != -1) continue
                        viewModel.attachmentsList[i].taskId = result.toInt()
                        val attachmentResult =
                            dbManager.insertAttachment(viewModel.attachmentsList[i])
                        if (attachmentResult < 0) {
                            Toast.makeText(
                                this,
                                "Nie udało sie dodać załącznika ${viewModel.attachmentsList[i].fileName}!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    notificationScheduler.scheduleNotification(newTask)
                    Toast.makeText(this, "Zadanie edytowano pomyślnie!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Błąd podczas edytowania zadania.", Toast.LENGTH_LONG)
                        .show()
                }
                return@setOnClickListener
            }

            val result = dbManager.insertTask(newTask)

            if (result > -1) {
                for (i in 0 until viewModel.attachmentsList.size) {
                    viewModel.attachmentsList[i].taskId = result.toInt()
                    val attachmentResult = dbManager.insertAttachment(viewModel.attachmentsList[i])
                    if (attachmentResult < 0) {
                        Toast.makeText(
                            this,
                            "Nie udało sie dodać załącznika ${viewModel.attachmentsList[i].fileName}!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                newTask.id = result.toInt()
                notificationScheduler.scheduleNotification(newTask)
                Toast.makeText(this, "Zadanie dodane pomyślnie!", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Błąd podczas dodawania zadania.", Toast.LENGTH_LONG).show()
            }

        }

        notificationsButton.setOnClickListener {
            viewModel.notificationOn = !viewModel.notificationOn
            setVisibilityNotification()
        }

        //Otwieranie dodaj załącznik
        addAttachmentButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "image/*"))
            }
            startActivityForResult(
                Intent.createChooser(intent, "Wybierz plik"),
                PICK_FILE_REQUEST_CODE
            )
        }

        dateNotificationInput.setOnClickListener { showDatePickerDialog(true) }
        timeNotificationInput.setOnClickListener { showTimePickerDialog(true) }
        planedDateInput.setOnClickListener { showDatePickerDialog(false) }
        planedTimeInput.setOnClickListener { showTimePickerDialog(false) }
    }

    @SuppressLint("SetTextI18n")
    private fun setVisibilityNotification() {
        val visibility = if (viewModel.notificationOn) VISIBLE else GONE

        notificationDateLabel.visibility = visibility
        notificationTimeLabel.visibility = visibility
        notificationTimeField.visibility = visibility
        notificationDateField.visibility = visibility
        timeNotificationInput.visibility = visibility
        dateNotificationInput.visibility = visibility

        if (viewModel.notificationOn) {
            notificationsButton.text = "Włączone"
            notificationsButton.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.add_task_background
                )
            )
            notificationsButton.background =
                ContextCompat.getDrawable(this, R.drawable.notifications_button)
            notificationIcon.setImageResource(R.drawable.notification_addtask)
        } else {
            notificationsButton.text = "Wyłączone"
            notificationsButton.setTextColor(ContextCompat.getColor(this, R.color.stroke))
            notificationsButton.background =
                ContextCompat.getDrawable(this, R.drawable.text_input_background)
            notificationIcon.setImageResource(R.drawable.no_notification)
        }

        if (!viewModel.notificationOn && (viewModel.notificationTime != "" || viewModel.notificationDate != "")) {
            viewModel.notificationTime = "Wybierz godzinę"
            viewModel.notificationDate = "Wybierz datę"
            notificationTimeField.text = viewModel.notificationTime
            notificationDateField.text = viewModel.notificationDate
            if (currentTaskId != -1) {
                task.notificationDate = null
                task.notificationTime = null
                dbManager.updateTask(task)
            }
        }
    }

    private fun validateCategory(): Boolean {
        if (category == "Wybierz kategorię") {
            Toast.makeText(this, "Nie wybrano kategorii!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun validateText(text: String, message: String): Boolean {
        if (text.isBlank()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun validateDate(): Boolean {
        if (viewModel.planedDate == "") {
            Toast.makeText(this, "Nie wybrałeś daty!", Toast.LENGTH_SHORT).show()
            return false
        }
        if (viewModel.planedTime == "") {
            Toast.makeText(this, "Nie wybrałeś godziny!", Toast.LENGTH_SHORT).show()
            return false
        }
        try {
            val formatter = DateTimeFormatter.ofPattern("d/M/yyyy H:mm")
            val selectedDateTime =
                LocalDateTime.parse("${viewModel.planedDate} ${viewModel.planedTime}", formatter)
            val now = LocalDateTime.now()

            if (selectedDateTime.isBefore(now)) {
                Toast.makeText(this, "Data musi być z przyszłości!", Toast.LENGTH_SHORT).show()
                return false
            }

            if (viewModel.notificationOn) {
                if (viewModel.notificationDate.isNotEmpty() && viewModel.notificationDate != "Wybierz datę") {
                    if (viewModel.notificationTime.isEmpty()) {
                        Toast.makeText(
                            this,
                            "Nie wybrano godziny powiadomienia!",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        return false
                    }

                    val notificationDateTime =
                        LocalDateTime.parse(
                            "${viewModel.notificationDate} ${viewModel.notificationTime}",
                            formatter
                        )

                    if (notificationDateTime.isBefore(now)) {
                        Toast.makeText(
                            this,
                            "Powiadomienie nie może być w przeszłości!",
                            Toast.LENGTH_SHORT
                        ).show()
                        return false
                    }

                    if (notificationDateTime.isAfter(selectedDateTime)) {
                        Toast.makeText(
                            this,
                            "Powiadomienie nie może być później niż zaplanowana data!",
                            Toast.LENGTH_SHORT
                        ).show()
                        return false
                    }
                } else {
                    if (viewModel.notificationTime.isNotEmpty() && viewModel.notificationTime != "Wybierz godzinę") {
                        Toast.makeText(this, "Nie wybrano daty powiadomienia!", Toast.LENGTH_SHORT)
                            .show()
                        return false
                    }
                }
            }
        } catch (_: Exception) {
            Toast.makeText(this, "Błąd formatu daty/godziny", Toast.LENGTH_SHORT).show()
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
                if (isNotification) {
                    viewModel.notificationDate = d
                    notificationDateField.text = viewModel.notificationDate
                } else {
                    viewModel.planedDate = d
                    planedDateField.text = viewModel.planedDate
                }
            },
            year, month, day
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
        datePickerDialog.show()
    }

    @SuppressLint("DefaultLocale")
    private fun showTimePickerDialog(isNotification: Boolean) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val t = String.format("%02d:%02d", selectedHour, selectedMinute)
                if (isNotification) {
                    viewModel.notificationTime = t
                    notificationTimeField.text = viewModel.notificationTime
                } else {
                    viewModel.planedTime = t
                    planedTimeField.text = viewModel.planedTime
                }
            },
            hour, minute, true
        )

        timePickerDialog.show()
    }

    private fun setSpinner() {
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
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedCategory = parent.getItemAtPosition(position).toString()
                category = selectedCategory
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun showDiscardChangesDialog() {
        AlertDialog.Builder(this)
            .setTitle("Porzucić zmiany?")
            .setMessage("Czy na pewno chcesz wyjść bez zapisywania zmian?")
            .setPositiveButton("Porzuć") { _, _ ->
                finish()
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            val uri: Uri? = data?.data
            if (uri != null) {
                handleFile(uri)
            }
        }
    }

    private fun handleFile(uri: Uri) {
        val fileName = getFileName(uri)
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

        val destinationFile = File(filesDir, fileName)
        try {
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (_: IOException) {
            Toast.makeText(this, "Błąd kopiowania pliku", Toast.LENGTH_SHORT).show()
            return
        }
        val attachment = Attachment(
            fileName = fileName,
            format = mimeType,
            localPath = destinationFile.absolutePath
        )
        viewModel.attachmentsList.add(attachment)
        if (viewModel.attachmentsList.isNotEmpty()) viewModel.hasAttachment = true
        addAttachmentView(attachment)
    }

    private fun addAttachmentView(attachment: Attachment) {
        val view = layoutInflater.inflate(R.layout.attachment, attachmentField, false)

        val attachmentName: TextView = view.findViewById(R.id.attachmentName)
        val deleteAttachmentButton: ImageView = view.findViewById(R.id.deleteAttachmentIcon)

        attachmentName.text = attachment.fileName

        attachmentName.setOnClickListener {
            openFile(attachment.localPath, attachment.format)
        }

        deleteAttachmentButton.setOnClickListener {
            //usuwanie załącznika
            val file = File(attachment.localPath)
            if (file.exists()) file.delete()

            attachmentField.removeView(view)

            if (attachment.taskId != -1) {
                dbManager.deleteAttachmentById(attachment.id)
            }
            viewModel.attachmentsList.remove(attachment)
            if (viewModel.attachmentsList.isEmpty()) viewModel.hasAttachment = false
        }

        attachmentField.addView(view)
    }

    @SuppressLint("Range")
    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result ?: "unknown"
    }

    private fun openFile(filePath: String, mimeType: String) {
        val file = File(filePath)
        if (!file.exists()) {
            Toast.makeText(this, "Plik nie istnieje!", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)

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
