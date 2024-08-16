package org.javaapp.dailylog.my

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import org.javaapp.dailylog.R
import org.javaapp.dailylog.SignInActivity
import org.javaapp.dailylog.databinding.FragmentMyBinding
import org.javaapp.dailylog.databinding.ItemMyBinding
import org.javaapp.dailylog.log.Log
import org.javaapp.dailylog.log.LogFragment
import org.javaapp.dailylog.user.User

class MyFragment : Fragment() {
    private lateinit var binding : FragmentMyBinding
    private lateinit var currentUser : FirebaseUser
    private lateinit var database : DatabaseReference

    private var onAddSelectedListener: OnAddSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        onAddSelectedListener = context as OnAddSelectedListener
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

                binding.myProfileImage.setImageResource(R.drawable.baseline_account_box_24)
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
    }

    private inner class MyLogHolder(private val binding : ItemMyBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(log : Log) {
            binding.myLogDateText.text = log.date
            binding.myLogTimeText.text = log.time
            if (log.image.isNullOrBlank()) {
                binding.myLogContentImage.setImageResource(R.drawable.baseline_home_filled_100) 
                // binding.myLogContentImage.isVisible = false
            } else {
                binding.myLogContentImage.apply {
                    setImageResource(R.drawable.baseline_image_48) // TODO 이미지 설정
                    binding.myLogContentImage.isVisible = true
                }
            }
            if (log.text.isNullOrBlank()) {
                binding.myLogContentText.isVisible = false
            } else {
                binding.myLogContentText.apply {
                    text = log.text
                    isVisible = true
                }
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
        val profileImage = dialog.findViewById<ImageView>(R.id.edit_profile_image)
        val nameEdit = dialog.findViewById<EditText>(R.id.edit_name_edit)
        val statusMessageEdit = dialog.findViewById<EditText>(R.id.edit_status_message_edit)
        val signOutButton = dialog.findViewById<Button>(R.id.edit_sign_out_button)

        // 로그아웃 버튼 리스너 설정
        signOutButton.setOnClickListener {
            Firebase.auth.signOut() // 로그아웃

            // 로그인 화면으로 이동
            val intent = Intent(requireActivity(), SignInActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        // 현재 데이터를 기본값으로 세팅
        profileImage.setImageResource(R.drawable.baseline_account_box_24)
        nameEdit.setText(binding.myNameText.text)
        statusMessageEdit.setText(binding.myStatusMessageText.text)



        // dialog 생성 및 띄우기, 이벤트 처리
        AlertDialog.Builder(requireContext())// 빌더
            .setTitle("내 정보 수정") // 제목
            .setView(dialog)// 뷰
            .setPositiveButton("저장") { dialog, _ -> // 저장 버튼을 눌렀을 때
                // 수정한 정보 가져오기
                val newName = nameEdit.text.toString()
                val newStatusMessage = statusMessageEdit.text.toString()

                // 파이어베이스 데이터베이스 유저 정보에 반영, 업데이트
                val userInfoUpdate = mapOf(
                    "name" to newName,
                    "statusMessage" to newStatusMessage
                )
                database.child(Key.DB_USERS).child(currentUser.uid).updateChildren(userInfoUpdate)

                // my 화면에 반영, 업데이트
                binding.myNameText.text = newName
                binding.myStatusMessageText.text = newStatusMessage

                // dialog 닫기
                dialog.dismiss()
            }.setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .create() // 생성
            .show() // 보여주기
    }
}