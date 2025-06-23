package com.example.todoapp

import androidx.lifecycle.ViewModel

class AddTaskViewModel : ViewModel(){
    var title: String = ""
    var description: String = ""
    var notificationTime: String = "Wybierz datę"
    var notificationDate: String = "Wybierz godzinę"
    var planedTime: String = "Wybierz datę"
    var planedDate: String = "Wybierz godzinę"
    var category: String = "Wybierz kategorię"
    var notificationOn: Boolean = false
    var hasAttachment: Boolean = false

    val attachmentsList: MutableList<Attachment> = mutableListOf()
}