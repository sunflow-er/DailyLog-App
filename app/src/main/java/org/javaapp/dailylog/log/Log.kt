package org.javaapp.dailylog.log

data class Log(
    val id : String? = null,
    val userId : String? = null,
    val date : String? = null,
    val time : String? = null,
    val text : String?= null,
    val image : String? = null,
    val likeCount : Int? = null,
    val commentCount : Int? = null,
)
