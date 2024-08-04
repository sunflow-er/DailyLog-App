package org.javaapp.dailylog.log

import org.javaapp.dailylog.with.User
import java.util.Date

data class Post(
    val postId : String? = null,
    val user : User? = null,
    val date : Date? = null,
    val text : String?= null,
    val image : Int? = null,
    val likeCount : Int? = null,
    val commentCount : Int? = null,
)
