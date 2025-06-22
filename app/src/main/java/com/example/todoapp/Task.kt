package com.example.todoapp

data class Task(
    val id : Int = 0,
    val title : String,
    val description : String,
    val createDate : String,
    val createTime : String,
    val planedDate : String,
    val planedTime : String,
    var endDate : String?,
    var endTime : String?,
    val category: String,
    val notificationDate : String?,
    val notificationTime : String?,
    val hasAttachments : Boolean,
    var isDone : Boolean
)
