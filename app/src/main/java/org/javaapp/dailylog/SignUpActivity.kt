package org.javaapp.dailylog

import android.content.Intent
import android.os.Bundle
import android.provider.SyncStateContract.Helpers.update
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.javaapp.dailylog.databinding.ActivitySignInBinding
import org.javaapp.dailylog.databinding.ActivitySignUpBinding

private const val TAG = "SignUpActivity"
private const val EMAIL = "email"
private const val PASSWORD = "password"

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.signUpButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.pwEditText.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                signUp(email, password) // 회원가입
            } else {
                Toast.makeText(this, "잘못된 이메일/비밀번호 형식", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signUp(email : String, password : String) {
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) { // 계정 생성에 성공하면
                Log.d(TAG, "createUserWithEmail : success")

                // 로그인 화면으로 이동
                val intent = Intent().apply {
                    putExtra(EMAIL, email)
                    putExtra(PASSWORD, password)
                }
                setResult(RESULT_OK, intent)
                finish()
                
            } else {
                Log.w(TAG, "createUserWithEmail : failure", task.exception)
                Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }
}