package com.example.todoapp

data class Task(
    val id : Int = 0,
    val title : String,
    val description : String,
    val createDate : String,
    val createTime : String,
    val endDate : String?,
    val endTime : String?,
    val category: String,
    val notificationDate : String?,
    val notificationTime : String?,
    val hasAttachments : Boolean,
    val isDone : Boolean
)
