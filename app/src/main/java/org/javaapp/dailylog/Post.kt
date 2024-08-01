package org.javaapp.dailylog

import java.util.Date

data class Post(
    val postId : String,
    val userId : String,
    val date : Date,
    val text : String,
    val image : Int,
    val likeCount : Int,
    val commentCount : Int,
)
