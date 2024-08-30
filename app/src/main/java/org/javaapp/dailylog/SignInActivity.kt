package org.javaapp.dailylog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Contacts.SettingsColumns.KEY
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.javaapp.dailylog.Key.Companion.DB_URL
import org.javaapp.dailylog.Key.Companion.DB_USERS
import org.javaapp.dailylog.databinding.ActivitySignInBinding

private const val TAG = "SignInActivity"
private const val EMAIL_KEY = "email"
private const val PASSWORD_KEY = "password"
private const val SIGN_UP = 1

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 로그인 버튼 리스너 설정
        binding.signInButton.setOnClickListener {
            val email = binding.emailEdit.text.toString()
            val password = binding.pwEdit.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signIn(email, password) // 로그인
            } else {
                Toast.makeText(this, "잘못된 이메일/비밀번호 형식", Toast.LENGTH_SHORT).show()
            }
        }

        // 회원가입 버튼 리스너 설정
        binding.signUpButton.setOnClickListener {
            signUp() // 회원가입
        }

    }

    private fun signIn(email: String, password: String) {
        Firebase.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            currentUser = Firebase.auth.currentUser!!

            if (task.isSuccessful) { // 로그인 성공
                // 메인 화면으로 이동
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // 로그인 액티비티 종료
            } else { // 로그인에 실패할 경우
                Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show() // 메시지 보여주기
            }
        }
    }

    private fun signUp() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivityForResult(intent, SIGN_UP)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SIGN_UP && resultCode == Activity.RESULT_OK) {
            // 회원가입 시에 입력한 이메일, 비밀번호 정보를 로그인 화면에 그대로 가져오기
            binding.emailEdit.setText(data?.getStringExtra(EMAIL_KEY))
            binding.pwEdit.setText(data?.getStringExtra(PASSWORD_KEY))
        }
    }
}
