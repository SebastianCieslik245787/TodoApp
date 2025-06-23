package com.example.todoapp

import TaskAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    private lateinit var fab : FloatingActionButton

    private lateinit var tasksField : RecyclerView
    private lateinit var adapter: TaskAdapter
    private lateinit var findQuery: EditText
    private lateinit var settingsButton: ImageView
    private lateinit var categorySpinner: Spinner
    private lateinit var sortingSpinner: Spinner
    private lateinit var showDoneTaskButton : ImageView
    private lateinit var executeFiltersButton: TextView

    private var category: String = "Wybierz kategorię..."
    private var query: String = ""
    private var sorting: String = "Wybierz sortowanie..."
    private var showDoneTasks : Boolean = false
    private var showDoneTasksFilter : Boolean = false

    private var tasks : List<Task> = listOf()

    private lateinit var dbManager : DatabaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        savedInstanceState?.let {
            category = it.getString("category", "Wybierz kategorię...")
            sorting = it.getString("sorting", "Wybierz sortowanie...")
            query = it.getString("query", "")
            showDoneTasks = it.getBoolean("showDoneTasks", showDoneTasks)
            showDoneTasksFilter = it.getBoolean("showDoneTasksFilter", showDoneTasksFilter)
        }

        setup()
        fillFields()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Zgoda na powiadomienia udzielona!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Brak zgody na powiadomienia", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadTasks()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("category", category)
        outState.putString("sorting", sorting)
        outState.putString("query", findQuery.text.toString())
        outState.putBoolean("showDoneTasks", showDoneTasks)
        outState.putBoolean("showDoneTasksFilter", showDoneTasksFilter)
    }

    private fun loadTasks() {

        tasks = dbManager.getTasks(
            sequence = if (query.isBlank()) null else query,
            category = if (category == "Wybierz kategorię" || category == "Wybierz kategorię...") null else category,
            sorted = sorting == "Najwcześniejsze" || sorting == "Najpóźniejsze",
            showDone = showDoneTasks
        )

        if (sorting == "Najpóźniejsze") {
            tasks = tasks.reversed()
        }

        adapter = TaskAdapter(tasks)
        tasksField.adapter = adapter
    }

    private fun setup(){
        dbManager = DatabaseManager(DatabaseHelper(this))
        fab = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, AddTask::class.java)
            startActivity(intent)
        }

        tasksField = findViewById(R.id.recyclerViewTasks)
        tasksField.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(tasks)
        tasksField.adapter = adapter

        findQuery = findViewById(R.id.taskQuery)
        settingsButton = findViewById(R.id.settingsButton)
        categorySpinner = findViewById(R.id.selectCategoryInputFilter)
        setCategorySpinner()
        sortingSpinner = findViewById(R.id.selectSortingInputFilter)
        setSortingSpinner()
        showDoneTaskButton = findViewById(R.id.showDoneTaskCheckBox)
        showDoneTaskButton.setOnClickListener {
            showDoneTasksFilter = !showDoneTasksFilter
            if(showDoneTasksFilter) showDoneTaskButton.setImageResource(R.drawable.checked)
            else showDoneTaskButton.setImageResource(R.drawable.unchecked)
        }
        executeFiltersButton = findViewById(R.id.confirmFiltersButton)
        executeFiltersButton.setOnClickListener {
            query = findQuery.text.toString()
            showDoneTasks = showDoneTasksFilter
            loadTasks()
        }
    }

    private fun fillFields(){
        findQuery.setText(query)
        if(showDoneTasksFilter) showDoneTaskButton.setImageResource(R.drawable.checked)
        else showDoneTaskButton.setImageResource(R.drawable.unchecked)
    }

    private fun setCategorySpinner() {
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

    private fun setSortingSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.sorting_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortingSpinner.adapter = adapter

        val spinnerPosition = adapter.getPosition(sorting)

        sortingSpinner.setSelection(spinnerPosition)

        sortingSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedSorting = parent.getItemAtPosition(position).toString()
                sorting = selectedSorting
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}