package org.javaapp.dailylog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.javaapp.dailylog.databinding.ActivitySignInBinding

private const val TAG = "SignInActivity"
private const val EMAIL = "email"
private const val PASSWORD = "password"
class SignInActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySignInBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent> // activity launcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth // Firebase auth
        activityResultLauncher = openActivityResultLauncher() // activity launcher

        // 로그인 버튼 리스너 설정
        binding.signInButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.pwEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signIn(email, password)
            } else {
                Toast.makeText(this, "잘못된 이메일/비밀번호 형식", Toast.LENGTH_SHORT).show()
            }
        }

        // 회원가입 버튼 리스너 설정
        binding.signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            activityResultLauncher.launch(intent) // 회원가입 액티비티 실행
        }

    }

    // ActivityResultLauncher 등록 및 초기화
    private fun openActivityResultLauncher() : ActivityResultLauncher<Intent> {
        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result : ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                binding.emailEditText.setText(result.data?.getStringExtra(EMAIL))
                binding.pwEditText.setText(result.data?.getStringExtra(PASSWORD))
            }
        }

        return resultLauncher
    }

    private fun signIn(email : String, password : String) {
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