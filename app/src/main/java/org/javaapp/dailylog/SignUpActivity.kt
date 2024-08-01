package org.javaapp.dailylog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.javaapp.dailylog.databinding.ActivitySignUpBinding

private const val TAG = "SignUpActivity"
private const val EMAIL_KEY = "email"
private const val PASSWORD_KEY = "password"
private const val NAME_KEY = "name"

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
                signUp(name, email, password) // 회원가입
            } else {
                Toast.makeText(this, "잘못된 이름/이메일/비밀번호 형식", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signUp(name : String, email : String, password : String) {
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) { // 계정 생성에 성공, 자동 로그인
                Log.d(TAG, "createUserWithEmail : success")

                // 회원가입 완료 시 해당 계정에 대한 정보를 firebase realtime database에 생성 및 저장
                val currentUser = auth.currentUser!!
                val currentUserId = currentUser.uid

                val user = mutableMapOf<String, Any>() // 사용자 정보를 저장할 맵 생성
                user["id"] = currentUserId
                user["name"] = name
                user["statusMessage"] = ""

                Firebase.database(Key.DB_URL).reference // 파이어베이스 데이터베이스에 사용자 정보를 업데이트
                    .child(Key.DB_USERS)
                    .child(currentUserId)
                    .updateChildren(user)

                // 로그인 화면에서 로그인을 진행하기 위해 로그아웃
                auth.signOut()

                // 로그인 화면으로 이동
                val intent = Intent().apply {
                    putExtra(EMAIL_KEY, email)
                    putExtra(PASSWORD_KEY, password)
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