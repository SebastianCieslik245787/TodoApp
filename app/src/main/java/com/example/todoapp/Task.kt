package com.example.todoapp

data class Task(
    var id : Int = 0,
    val title : String,
    val description : String,
    val createDate : String,
    val createTime : String,
    val planedDate : String,
    val planedTime : String,
    var endDate : String?,
    var endTime : String?,
    val category: String,
    var notificationDate : String?,
    var notificationTime : String?,
    var hasAttachments : Boolean,
    var isDone : Boolean,
    var notificationOn : Boolean = false
)
