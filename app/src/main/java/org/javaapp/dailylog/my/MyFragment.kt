package org.javaapp.dailylog.my

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import org.javaapp.dailylog.OnAddSelectedListener
import org.javaapp.dailylog.OnLogSelectedListener
import org.javaapp.dailylog.R
import org.javaapp.dailylog.SignInActivity
import org.javaapp.dailylog.databinding.FragmentMyBinding
import org.javaapp.dailylog.databinding.ItemMyBinding
import org.javaapp.dailylog.log.Log
import org.javaapp.dailylog.log.LogFragment
import org.javaapp.dailylog.user.User
import java.io.File

private const val PICK_IMAGE_REQUEST = 1

class MyFragment : Fragment() {
    private lateinit var binding : FragmentMyBinding
    private lateinit var currentUser : FirebaseUser
    private lateinit var database : DatabaseReference
    private lateinit var profileImage : ImageView
    private var imageUri : Uri? = null // 프로필 이미지의 URI

    private var onAddSelectedListener: OnAddSelectedListener? = null
    private var onLogSelectedListener : OnLogSelectedListener? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)

        onAddSelectedListener = context as OnAddSelectedListener
        onLogSelectedListener = context as OnLogSelectedListener
    }

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
        binding = FragmentMyBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.myLogRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = MyLogAdapter(emptyList())
        }

        // 앱바 게시글 추가 메뉴 설정
        val menuHost : MenuHost = requireActivity()
        val menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_my, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when(menuItem.itemId) {
                    R.id.add_log -> {
                        onAddSelectedListener?.onAddSelected()
                        true
                    }
                    else -> false
                }
            }

        }
        menuHost.addMenuProvider(menuProvider, viewLifecycleOwner)


        // 내 정보 가져와서 띄우기
        database.child(Key.DB_USERS).child(currentUser.uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                user ?: return

                imageUri = user.profileImage?.toUri()
                Glide.with(requireContext()).load(user.profileImage).into(binding.myProfileImage) // 프로필 이미지
                binding.myNameText.text = user.name
                binding.myStatusMessageText.text = user.statusMessage
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        // 내 로그 가져와서 띄우기
        // equalTo 메서드는 orderByChild 메서드와 함께 사용해야 한다.
        database.child(Key.DB_LOGS).orderByChild("userId").equalTo(currentUser.uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val myLogList = mutableListOf<Log>()

                snapshot.children.forEach {
                    val myLog = it.getValue(Log::class.java)
                    myLog ?: return

                    myLogList.add(myLog)
                }

                // 생성 시간을 기준으로 정렬
                myLogList.sortByDescending { it.timeStamp }

                binding.myLogRecyclerView.adapter = MyLogAdapter(myLogList)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        // 내 정보 수정 버튼 리스너 설정
        binding.myEditButton.setOnClickListener {
            showEditProfileDialog()
        }

    }

    override fun onDetach() {
        super.onDetach()
        onAddSelectedListener = null
        onLogSelectedListener = null
    }

    private inner class MyLogHolder(private val binding : ItemMyBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(log : Log) {
            binding.myLogDateText.text = log.date
            binding.myLogTimeText.text = log.time
            if (log.image.isNullOrBlank()) {
                binding.myLogContentImage.isVisible = false
            } else {
                binding.myLogContentImage.isVisible = true

                // 이미지 띄우기
                Glide.with(requireContext())
                    .load(log.image)
                    .into(binding.myLogContentImage)
            }
            if (log.text.isNullOrBlank()) {
                binding.myLogContentText.isVisible = false
            } else {
                binding.myLogContentText.apply {
                    text = log.text
                    isVisible = true
                }
            }

            // 리스너 설정
            binding.root.setOnClickListener {
                onLogSelectedListener?.onLogSelected(log.id!!)
            }
        }
    }

    private inner class MyLogAdapter(private val myLogList : List<Log>) : RecyclerView.Adapter<MyLogHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyLogHolder {
            val binding = ItemMyBinding.inflate(layoutInflater, parent, false)

            return MyLogHolder(binding)
        }

        override fun getItemCount(): Int {
            return myLogList.size
        }
        override fun onBindViewHolder(holder: MyLogHolder, position: Int) {
            holder.bind(myLogList[position])
        }

    }

    companion object {
        fun newInstance() : MyFragment {
            return MyFragment()
        }
    }

    private fun showEditProfileDialog() {
        // dialog 생성에 필요한 뷰 정보들
        val dialog = LayoutInflater.from(context).inflate(R.layout.dialog_edit_my, null)
        profileImage = dialog.findViewById<ImageView>(R.id.edit_profile_image)
        val nameEdit = dialog.findViewById<EditText>(R.id.edit_name_edit)
        val statusMessageEdit = dialog.findViewById<EditText>(R.id.edit_status_message_edit)
        val signOutButton = dialog.findViewById<Button>(R.id.edit_sign_out_button)

        // 현재 데이터를 기본값으로 세팅
        Glide.with(this).load(imageUri).into(profileImage)
        nameEdit.setText(binding.myNameText.text)
        statusMessageEdit.setText(binding.myStatusMessageText.text)

        // 프로필 이미지 선택 리스너 설정
        profileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // 로그아웃 버튼 리스너 설정
        signOutButton.setOnClickListener {
            Firebase.auth.signOut() // 로그아웃

            // 로그인 화면으로 이동
            val intent = Intent(requireActivity(), SignInActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        // dialog 생성 및 띄우기, 이벤트 처리
        AlertDialog.Builder(requireContext())// 빌더
            .setTitle("내 정보 수정") // 제목
            .setView(dialog)// 뷰
            .setPositiveButton("저장") { dialog, _ -> // 저장 버튼을 눌렀을 때
                // 수정한 정보 가져오기
                val newName = nameEdit.text.toString()
                val newStatusMessage = statusMessageEdit.text.toString()
                val newProfileImage = imageUri.toString()

                // 파이어베이스 데이터베이스 유저 정보에 반영, 업데이트
                val userInfoUpdate = mapOf(
                    "name" to newName,
                    "statusMessage" to newStatusMessage,
                    "profileImage" to newProfileImage
                )
                database.child(Key.DB_USERS).child(currentUser.uid).updateChildren(userInfoUpdate)

                // dialog 닫기
                dialog.dismiss()
            }.setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .create() // 생성
            .show() // 보여주기
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                imageUri = uri

                // 선택된 이미지 띄우기
                Glide.with(this).load(uri).into(profileImage)
            }
        }
    }

}