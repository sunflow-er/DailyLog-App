package org.javaapp.dailylog

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth // FirebaseAuth 객체의 공유 인스턴스 가져오기
    }

    override fun onStart() {
        super.onStart()

        // 사용자가 현재 로그인되어 있는지 확인
        val currentUser : FirebaseUser? = auth.currentUser // 현재 사용자
        if (currentUser != null) { // 로그인 되어있으면
            // reload() // UI 업데이트
        } else { // 로그인 되어있지 않으면
            // 로그인 화면으로 이동
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish() // 현재 액티비티 종료
        }
    }
}