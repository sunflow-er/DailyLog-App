package org.javaapp.dailylog

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.javaapp.dailylog.databinding.ActivitySignInBinding

private const val TAG = "SignInActivity"

class SignInActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySignInBinding
    private lateinit var auth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth // Firebase auth

        // 로그인 버튼 리스너 설정
        binding.signInButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.pwEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signIn(email, password)
            }
        }

    }

    fun signIn(email : String, password : String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) { // 로그인 성공
                Log.d(TAG, "signInWithEmail : success")
                val user = auth.currentUser
                // updateUI(user) // UI 업데이트
            } else { // 로그인에 실패할 경우
                Log.w(TAG, "signInWithEmail : failure", task.exception)
                Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show() // 메시지 보여주기
            }
        }
    }
}