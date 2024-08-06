package org.javaapp.dailylog

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

// 파이어베이스 데이터베이스로부터 사용자 이름(name)을 비동기적으로 가져오고 UI에 반영하기 위한 콜백 인터페이스
interface UserNameCallback {
    fun onUserNameRetrieved(userName : String?)
}

// 파이어베이스 데이터베이스에서 사용자 id에 해당하는 이름 가져오기
fun getUserName(database : DatabaseReference, userId : String, callback : UserNameCallback) {
    database.child(Key.DB_USERS).child(userId).child("name")
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName = snapshot.getValue(String::class.java)
                callback.onUserNameRetrieved(userName)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onUserNameRetrieved("알 수 없음")
            }

        })
}