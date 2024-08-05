package org.javaapp.dailylog.log

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.javaapp.dailylog.Key
import org.javaapp.dailylog.databinding.FragmentAddLogBinding
import java.time.LocalDateTime

class AddLogFragment : Fragment() {

    private lateinit var binding : FragmentAddLogBinding
    private lateinit var currentUser : FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentUser = Firebase.auth.currentUser!!
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

        binding.addImage.setOnClickListener {
            // TODO 이미지 불러오기
        }

        // 업로드 버튼 리스너 설정
        binding.addButton.setOnClickListener {

            // 저장할 로그 정보
            val log = mutableMapOf<String, Any>()
            log["userId"] = currentUser.uid
            log["date"] = LocalDateTime.now().toString()
            log["text"] = binding.addTextEdit.text.toString()
            log["image"] = ""
            log["likeCount"] = 0
            log["commentCount"] = 0

            // 파이어베이스 데이터베이스에 로그 정보 저장
            Firebase.database(Key.DB_URL).reference
                .child(Key.DB_LOGS)
                .push()
                .setValue(log)

            // 프래그먼트 종료
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    
}