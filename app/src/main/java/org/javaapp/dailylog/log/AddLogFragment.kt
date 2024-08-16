package org.javaapp.dailylog.log

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.javaapp.dailylog.Key
import org.javaapp.dailylog.R
import org.javaapp.dailylog.databinding.FragmentAddLogBinding
import org.javaapp.dailylog.formatDateTimeNow
import org.javaapp.dailylog.formatText
import java.util.UUID

private const val PICK_IMAGE_REQUEST = 1

class AddLogFragment : Fragment() {

    private lateinit var binding : FragmentAddLogBinding
    private lateinit var currentUser : FirebaseUser // user
    private lateinit var database : DatabaseReference // database

    private var formattedText : String = ""// 업로드할 텍스트
    private var imageUri : Uri? = null // 업로드할 이미지의 URI

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
            openGallery() // 갤러리 열기
        }

        // 업로드 버튼 리스너 설정
        binding.addButton.setOnClickListener {
            formattedText = formatText(binding.addTextEdit.text.toString())

            if (imageUri == null && formattedText.isNullOrBlank()) {
                Toast.makeText(requireContext(), "내용을 입력해주세요", Toast.LENGTH_SHORT).show()
            } else {
                val logId = UUID.randomUUID().toString() // 로그 아이디
                val timeStamp = System.currentTimeMillis().toString()
                val (date, time) = formatDateTimeNow() // 포맷팅된 현재 날짜 및 시간

                // 저장할 로그 정보
                val log = mutableMapOf<String, Any>()
                log["id"] = logId
                log["userId"] = currentUser.uid
                log["date"] = date
                log["time"] = time
                log["text"] = formattedText
                log["image"] = imageUri?.toString() ?: ""
                log["likeCount"] = 0
                log["commentCount"] = 0
                log["timeStamp"] = timeStamp

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

    // 갤러리 열기 함수
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST) // 갤러리 열기
    }

    // 갤러리에서 선택한 이미지 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                imageUri = uri
                loadImageFromUri(uri)
            }
        }
    }
    
    // 선택한 이미지의 URI를 ImageView에 업로드
    private fun loadImageFromUri(uri : Uri) {
        Glide.with(this) // Context
            .load(uri) // URI
            .into(binding.addImage) // View
    }


    
}