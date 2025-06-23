package com.example.todoapp

import TaskAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var fab : FloatingActionButton

    private lateinit var tasksField : RecyclerView
    private lateinit var adapter: TaskAdapter

    private var tasks : List<Task> = listOf()

    private lateinit var dbManager : DatabaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        setup()

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

    private fun loadTasks() {
        tasks = dbManager.getTasks()

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
    }
}