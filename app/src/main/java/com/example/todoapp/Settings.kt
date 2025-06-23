package com.example.todoapp

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit

class Settings : AppCompatActivity(){
    private lateinit var backButton : ImageView
    private lateinit var selectHourIcon : ImageView
    private lateinit var selectMinuteIcon : ImageView
    private lateinit var hoursSpinner : Spinner
    private lateinit var minutesSpinner : Spinner
    private lateinit var saveSettings : TextView

    private var hours : String = ""
    private var minutes : String = ""

    private lateinit var sharedPref: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.settings)
        sharedPref = getSharedPreferences("settings", MODE_PRIVATE)

        hours = sharedPref.getString("hours", "0").toString()
        minutes = sharedPref.getString("minutes", "1").toString()

        setup()
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

    private fun setup(){
        backButton = findViewById(R.id.settingsBackButton)
        backButton.setOnClickListener { finish() }

        hoursSpinner = findViewById(R.id.selectHourInputSettings)
        setHourSpinner()

        minutesSpinner = findViewById(R.id.selectMinuteInputSettings)
        setMinuteSpinner()

        saveSettings = findViewById(R.id.confirmSettingsButton)
        saveSettings.setOnClickListener {
            sharedPref.edit {
                putString("hours", hours)
                putString("minutes", hours)
            }
        }

        selectHourIcon = findViewById(R.id.selectHourInputIconSettings)
        selectMinuteIcon = findViewById(R.id.selectMinuteInputIconSettings)
    }

    private fun setHourSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.hour_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        hoursSpinner.adapter = adapter

        val spinnerPosition = adapter.getPosition(hours.toString())

        hoursSpinner.setSelection(spinnerPosition)

        hoursSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedCategory = parent.getItemAtPosition(position).toString()
                hours = selectedCategory
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setMinuteSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.min_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        minutesSpinner.adapter = adapter

        val spinnerPosition = adapter.getPosition(minutes.toString())

        minutesSpinner.setSelection(spinnerPosition)

        minutesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedCategory = parent.getItemAtPosition(position).toString()
                minutes = selectedCategory
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}