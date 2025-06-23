package com.example.todoapp

import TaskAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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