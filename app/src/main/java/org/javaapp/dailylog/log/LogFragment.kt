package org.javaapp.dailylog.log

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
import org.javaapp.dailylog.R
import org.javaapp.dailylog.databinding.FragmentLogBinding
import org.javaapp.dailylog.databinding.ItemLogBinding


class LogFragment : Fragment() {

    // 프래그먼트에서 이벤트를 전달하기 위한 리스너 인터페이스 정의
    interface OnLogSelectedListener { // 게시글이 선택되었을 때
        fun onLogSelected()
    }
    interface OnAddSelectedListener { // 앱바 메뉴의 게시
        // 글 추가 버튼이 선택되었을 때
        fun onAddSelected()
    }

    private lateinit var binding : FragmentLogBinding
    private lateinit var currentUser : FirebaseUser
    private lateinit var database : DatabaseReference

    private var onLogSelectedListener : OnLogSelectedListener? = null
    private var onAddSelectedListener : OnAddSelectedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentUser = Firebase.auth.currentUser!!
        database = Firebase.database(Key.DB_URL).reference
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        onAddSelectedListener = context as OnAddSelectedListener
        onLogSelectedListener = context as OnLogSelectedListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLogBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 앱바 메뉴 인플레이트 및 리스너 설정
        val menuHost : MenuHost = requireActivity() // 메뉴를 관리하는 호스트
        val menuProvider = object : MenuProvider { // 메뉴호스트에 메뉴프로바이더 추가, 메뉴 생성 및 항목 선택 처리

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) { // 메뉴 생성 시 호출
                menuInflater.inflate(R.menu.menu_log, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean { // 메뉴 아이템 선택 시 호출
                return when (menuItem.itemId) {
                    R.id.add_log -> {
                        onAddSelectedListener?.onAddSelected() // 새 게시글 작성 프래그먼트로 이동
                        true
                    }
                    else -> false
                }
            }

        }
        menuHost.addMenuProvider(menuProvider, viewLifecycleOwner)

        // 리사이클러뷰 설정
        binding.logRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = LogAdapter(emptyList())
        }

        // 파이어베이스 데이터베이스에서 로그 정보 가져오기 (업데이트 될때마다)
        database.child(Key.DB_LOGS).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val logList = mutableListOf<Log>()

                snapshot.children.forEach {
                    val log = it.getValue(Log::class.java)
                    log ?: return

//                    if (log.userId != currentUser.uid) {
//                        logList.add(log)
//                    }
                }

                binding.logRecyclerView.adapter = LogAdapter(logList)
            }

            override fun onCancelled(error: DatabaseError) {
                // TODO
            }

        })


    }

    override fun onDetach() {
        super.onDetach()
        onAddSelectedListener = null
        onLogSelectedListener = null
    }

    private inner class LogHolder(private val binding : ItemLogBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                onLogSelectedListener?.onLogSelected()
            }

            binding.logLikeButton.setOnClickListener {
                // TODO likeCount++
            }

            binding.logCommentButton.setOnClickListener {
                onLogSelectedListener?.onLogSelected()
            }
        }


        fun bind(log : Log) {

            binding.logUserProfileImage.setImageResource(R.drawable.baseline_account_box_24) // 프로필 이미지
            binding.logUserNameText.setText(getUserName(log)) // 사용자 이름
            binding.logDateText.setText(log.date) // 게시 날짜
            binding.logTimeText.setText(log.date) // 게시 시간
            if (log.image.isNullOrBlank()) { // 사진을 첨부하지 않았으면
                binding.logContentImage.isVisible = false // 보이지 않게
            } else { // 첨부했으면
                binding.logContentImage.apply {
                    setImageResource(R.drawable.baseline_image_48) // 보이게
                    isVisible = true
                }
            }
            if (log.text.isNullOrBlank()) { // 글을 작성하지 않았으면
                binding.logContentText.isVisible = false // 보이지 않게
            } else { // 작성했다면
                binding.logContentText.apply {
                    setText(log.text) // 보이게
                    isVisible = true
                }
            }

        }

    }

    private inner class LogAdapter(private val logList : List<Log>) : RecyclerView.Adapter<LogHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogHolder {
            val binding = ItemLogBinding.inflate(layoutInflater, parent, false)

            return LogHolder(binding)
        }

        override fun getItemCount(): Int {
            return logList.size
        }

        override fun onBindViewHolder(holder: LogHolder, position: Int) {
            holder.bind(logList[position])
        }

    }

    companion object {
        fun newInstance() : LogFragment {
            return LogFragment()
        }
    }

    private fun getUserName(log : Log) : String? {
        var userName : String? = null

        database.child(Key.DB_USERS).child(log.userId!!).child("name")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userName = snapshot.getValue(String::class.java)
                }

                override fun onCancelled(error: DatabaseError) {
                    userName = "알 수 없음"
                }
            })

        return userName
    }
}