package org.javaapp.dailylog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.javaapp.dailylog.databinding.ActivitySignUpBinding

private const val TAG = "SignUpActivity"
private const val EMAIL_KEY = "email"
private const val PASSWORD_KEY = "password"
private const val NAME_KEY = "name"

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var currentUser : FirebaseUser
    private lateinit var database : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signUpButton.setOnClickListener {
            val name = binding.nameEdit.text.toString()
            val email = binding.emailEdit.text.toString()
            val password = binding.pwEditText.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                signUp(name, email, password) // 회원가입
            } else {
                Toast.makeText(this, "잘못된 이름/이메일/비밀번호 형식", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signUp(name : String, email : String, password : String) {
        Firebase.auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) { // 계정 생성에 성공, 자동 로그인
                currentUser = Firebase.auth.currentUser!! // 현재 사용자 계정
                database = Firebase.database(Key.DB_URL).reference // 데이터베이스

                // 회원가입 완료 시 해당 계정에 대한 정보를 firebase realtime database에 생성 및 저장
                val user = mutableMapOf<String, Any>() // 사용자 정보를 저장할 맵 생성
                user["id"] = currentUser.uid
                user["name"] = name
                user["statusMessage"] = ""
                user["profileImage"] = ""

                database // 파이어베이스 데이터베이스에 사용자 정보를 업데이트
                    .child(Key.DB_USERS)
                    .child(currentUser.uid)
                    .updateChildren(user)

                // 로그인 화면에서 로그인을 진행하기 위해 로그아웃
                Firebase.auth.signOut()

                // 로그인 화면으로 이동
                val intent = Intent().apply {
                    putExtra(EMAIL_KEY, email)
                    putExtra(PASSWORD_KEY, password)
                }
                setResult(RESULT_OK, intent)
                finish()
                
            } else {
                Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

}