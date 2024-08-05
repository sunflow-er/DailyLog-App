package org.javaapp.dailylog

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.javaapp.dailylog.databinding.ActivityMainBinding
import org.javaapp.dailylog.log.AddLogFragment
import org.javaapp.dailylog.log.LogFragment
import org.javaapp.dailylog.my.MyFragment
import org.javaapp.dailylog.user.UserFragment

class MainActivity : AppCompatActivity(), LogFragment.OnAddSelectedListener, LogFragment.OnLogSelectedListener {
    private lateinit var binding : ActivityMainBinding
    private lateinit var auth: FirebaseAuth // firebase auth

    private lateinit var userFragment: UserFragment
    private lateinit var logFragment: LogFragment
    private lateinit var myFragment: MyFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth // FirebaseAuth 객체의 공유 인스턴스 가져오기

        // 프래그먼트 객체 초기화(생성)
        userFragment = UserFragment.newInstance()
        logFragment = LogFragment.newInstance()
        myFragment = MyFragment.newInstance()

        // 프래그먼트 컨테이너 초기화(
        // 설정)
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) // 현재 프래그먼트 정보 가져오기
        if (currentFragment == null) { // 현재 프래그먼트가 없다면
            val baseFragment = userFragment // WithFragment를 기본 프래그먼트로 설정
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, baseFragment).commit() // 기본 프래그먼트 띄우기
        }

        // 바텀 내비게이션 뷰 리스너 설정
        binding.bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.fragment_user -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, userFragment).commit()
                    true
                }
                R.id.fragment_log -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, logFragment).commit()
                    true
                }
                R.id.fragment_my -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, myFragment).commit()
                    true
                }
                else -> false
            }
        }
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

    override fun onLogSelected() {
        TODO("Not yet implemented")
    }

    override fun onAddSelected() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AddLogFragment())
            .addToBackStack(null)
            .commit()
    }


}