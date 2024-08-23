package org.javaapp.dailylog

interface OnLogSelectedListener { // 게시글이 선택되었을 때
    fun onLogSelected(logId: String)
}