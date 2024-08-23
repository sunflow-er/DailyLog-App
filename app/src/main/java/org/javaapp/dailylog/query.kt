package org.javaapp.dailylog

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import org.javaapp.dailylog.user.User

// 파이어베이스 데이터베이스로부터 사용자 이름(name)을 비동기적으로 가져오고 UI에 반영하기 위한 콜백 인터페이스
interface UserInfoCallback {
    fun onUserInfoRetrieved(userInfo : User)
}

// 파이어베이스 데이터베이스에서 사용자 id에 해당하는 이름 가져오기
fun getUserInfo(database : DatabaseReference, userId : String, callback : UserInfoCallback) {
    database.child(Key.DB_USERS).child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userInfo = snapshot.getValue(User::class.java)
                callback.onUserInfoRetrieved(userInfo!!)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
}