package org.javaapp.dailylog.log

import org.javaapp.dailylog.user.User
import java.util.Date

data class Log(
    val postId : String? = null,
    val userId : String? = null,
    val date : String? = null,
    val text : String?= null,
    val image : String? = null,
    val likeCount : Int? = null,
    val commentCount : Int? = null,
)
