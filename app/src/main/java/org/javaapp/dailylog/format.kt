package org.javaapp.dailylog

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// 게시글 작성 내용 포맷
fun formatText(text : String) : String {
    val formattedText = removeNewLine(text.trim())

    return formattedText
}

// 의미 없는 줄바꿈 제거
fun removeNewLine(input : String) : String {
   while(input.endsWith("\n")) {
       input.dropLast(1)
   }

    return input
}

// 현재 날짜 및 시간 포맷팅
fun formatDateTimeNow() : Pair<String, String> {
    // 현재 날짜 및 시간
    val dateTimeNow = LocalDateTime.now()

    // 날짜 포맷팅
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd (E)", Locale.KOREAN)
    val formattedDate = dateTimeNow.format(dateFormatter)

    // 시간 포맷팅
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val formattedTime = dateTimeNow.format(timeFormatter)

    return Pair(formattedDate, formattedTime)
}