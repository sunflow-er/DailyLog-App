package org.javaapp.dailylog.log

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.javaapp.dailylog.Key
import org.javaapp.dailylog.R
import org.javaapp.dailylog.databinding.FragmentAddLogBinding
import org.javaapp.dailylog.formatDateTimeNow
import org.javaapp.dailylog.formatText
import java.util.UUID

private const val PICK_IMAGE_REQUEST = 1

class AddEditLogFragment(private val logId : String? = null) : Fragment() {
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

        if (logId != null) { // Edit
            fetchMyLog(logId)
        }

        setupAppBarMenu() // 앱바 메뉴 인플레이트 및 리스너 설정

        // 이미지 추가하기
        binding.addImage.setOnClickListener {
            addImage() // 갤러리에서 이미지 선택하여 추가
        }

        // 업로드 버튼 리스너 설정
        binding.addButton.setOnClickListener {
            uploadLog(logId)
        }
    }

    private fun setupAppBarMenu() {
        val menuHost : MenuHost = requireActivity()
        val menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_comment_log, menu)
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
    }

    private fun addImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST) // 갤러리 열기
    }

    private fun uploadLog(logId : String?) {
        formattedText = formatText(binding.addTextEdit.text.toString())

        if (imageUri == null && formattedText.isNullOrBlank()) {
            Toast.makeText(requireContext(), "내용을 입력해주세요", Toast.LENGTH_SHORT).show()
        } else {
            if (logId != null) { // Edit
                val logEdit = mutableMapOf<String, Any>()
                logEdit["text"] = formattedText
                logEdit["image"] = imageUri?.toString() ?: ""

                database.child(Key.DB_LOGS).child(logId).updateChildren(logEdit)

            } else { // Add
                val newLogId = UUID.randomUUID().toString() // 로그 아이디
                val (date, time) = formatDateTimeNow() // 포맷팅된 현재 날짜 및 시간
                val timeStamp = System.currentTimeMillis().toString() // 타임스탬프

                // 저장할 로그 정보
                val logAdd = mutableMapOf<String, Any>()
                logAdd["id"] = newLogId
                logAdd["userId"] = currentUser.uid
                logAdd["date"] = date
                logAdd["time"] = time
                logAdd["text"] = formattedText
                logAdd["image"] = imageUri?.toString() ?: ""
                logAdd["likeCount"] = 0
                logAdd["commentCount"] = 0
                logAdd["timeStamp"] = timeStamp

                // 파이어베이스 데이터베이스에 로그 정보 저장
                database.child(Key.DB_LOGS).child(newLogId).setValue(logAdd)
            }

            // 프래그먼트 종료
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun fetchMyLog(logId: String) {
        database.child(Key.DB_LOGS).child(logId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isAdded) { // 프래그먼트가 현재 액티비티에 추가(연결)되었는지 확인, 프래그먼트가 활성 상태인지 확인
                    val log = snapshot.getValue(Log::class.java)
                    log ?: return

                    // 기존 이미지
                    if (log.image.isNullOrBlank()) {
                        binding.addImage.setImageResource(R.drawable.baseline_image_48)
                    } else {
                        Glide.with(requireContext()).load(log.image).into(binding.addImage)
                    }

                    // 기존 텍스트
                    binding.addTextEdit.setText(log.text)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    // 갤러리에서 선택한 이미지 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                imageUri = uri
                Glide.with(this) // Context
                    .load(uri) // URI
                    .into(binding.addImage) // View
            }
        }
    }
}