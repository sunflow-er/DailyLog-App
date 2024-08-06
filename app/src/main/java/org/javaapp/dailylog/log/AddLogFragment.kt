package org.javaapp.dailylog.log

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.javaapp.dailylog.Key
import org.javaapp.dailylog.R
import org.javaapp.dailylog.databinding.FragmentAddLogBinding
import java.time.LocalDateTime
import java.util.UUID

class AddLogFragment : Fragment() {

    private lateinit var binding : FragmentAddLogBinding
    private lateinit var currentUser : FirebaseUser // user
    private lateinit var database : DatabaseReference // database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentUser = Firebase.auth.currentUser!!
        database = Firebase.database(Key.DB_URL).reference
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddLogBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 앱바 메뉴 인플레이트 및 리스너 설정
        val menuHost : MenuHost = requireActivity()
        val menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_comment, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.back_to_log -> {
                        requireActivity().supportFragmentManager.popBackStack() // 뒤로 가기
                        true
                    }
                    else -> false
                }
            }
        }
        menuHost.addMenuProvider(menuProvider, viewLifecycleOwner)

        binding.addImage.setOnClickListener {
            // TODO 이미지 불러오기
        }

        // 업로드 버튼 리스너 설정
        binding.addButton.setOnClickListener {

            // 저장할 로그 정보
            val logId = UUID.randomUUID().toString()

            val log = mutableMapOf<String, Any>()
            log["id"] = logId
            log["userId"] = currentUser.uid
            log["date"] = LocalDateTime.now().toString()
            log["text"] = binding.addTextEdit.text.toString()
            log["image"] = ""
            log["likeCount"] = 0
            log["commentCount"] = 0

            // 파이어베이스 데이터베이스에 로그 정보 저장
            database
                .child(Key.DB_LOGS)
                .child(logId)
                .setValue(log)

            // 프래그먼트 종료
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    
}