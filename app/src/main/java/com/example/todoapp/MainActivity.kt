package com.example.todoapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var fab : FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        setup()
    }

    private fun setup(){
        fab = findViewById(R.id.fab)
        fab.setOnClickListener { val intent = Intent(this, AddTask::class.java)
            startActivity(intent) }
    }
}