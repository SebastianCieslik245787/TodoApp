package com.example.todoapp

data class Attachment(
    var id : Int = 0,
    var taskId : Int = 0,
    var format : String,
    var fileName : String,
    val localPath: String
)
