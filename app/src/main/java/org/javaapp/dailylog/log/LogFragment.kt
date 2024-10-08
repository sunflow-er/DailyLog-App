package org.javaapp.dailylog.log

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserInfo
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
import org.javaapp.dailylog.UserInfoCallback
import org.javaapp.dailylog.databinding.FragmentLogBinding
import org.javaapp.dailylog.databinding.ItemLogBinding
import org.javaapp.dailylog.getUserInfo
import org.javaapp.dailylog.user.User


class LogFragment : Fragment() {

    private lateinit var binding: FragmentLogBinding
    private lateinit var currentUser: FirebaseUser
    private lateinit var database: DatabaseReference

    private var onLogSelectedListener: OnLogSelectedListener? = null
    private var onAddSelectedListener: OnAddSelectedListener? = null

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
        binding = FragmentLogBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 리사이클러뷰 설정
        binding.logRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = LogAdapter(emptyList())
        }

        setupAppBarMenu() // 앱바 메뉴 인플레이트 및 리스너 설정

        fetchLogList() // 파이어베이스 데이터베이스에서 로그 정보 가져오기 (업데이트 될때마다)

    }

    override fun onDetach() {
        super.onDetach()
        onAddSelectedListener = null
        onLogSelectedListener = null
    }

    private inner class LogHolder(private val binding: ItemLogBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(log: Log) {
            // 로그 디테일 설정
            // 로그 작성자 이름, 프로필 이미지
            getUserInfo(database, log.userId!!, object : UserInfoCallback {
                override fun onUserInfoRetrieved(userInfo: User) {
                    binding.logUserNameText.text = userInfo.name // 로그 작성자 이름
                    if (userInfo.profileImage.isNullOrBlank()) {
                        binding.logUserProfileImage.setImageResource(R.drawable.baseline_account_box_24)
                    } else {
                        Glide.with(requireContext()).load(userInfo.profileImage).into(binding.logUserProfileImage) // 로그 작성자 프로필 이미지
                    }

                }
            })

            // 게시 날짜 및 시간
            binding.logDateText.setText(log.date) // 게시 날짜
            binding.logTimeText.setText(log.time) // 게시 시간

            // 로그 이미지
            if (log.image.isNullOrBlank()) { // 사진을 첨부하지 않았으면
                binding.logContentImage.isVisible = false // 보이지 않게
            } else { // 첨부했으면
                binding.logContentImage.isVisible = true

                // 이미지 업로드
                Glide.with(requireContext())
                    .load(log.image.toUri())
                    .into(binding.logContentImage)

            }

            // 로그 텍스트
            if (log.text.isNullOrBlank()) { // 글을 작성하지 않았으면
                binding.logContentText.isVisible = false // 보이지 않게
            } else { // 작성했다면
                binding.logContentText.apply {
                    text = log.text
                    isVisible = true
                }
            }

            // 이벤트 리스너 설정
            // 로그 화면 전체
            binding.root.setOnClickListener {// 로그 화면(전체) 클릭 시
                onLogSelectedListener?.onLogSelected(log.id!!) // CommentLogFragment로 이동
            }

            // 좋아요 버튼
            binding.logLikeButton.setOnClickListener {// 하트 버튼 클릭 시
                log.likeCount = log.likeCount?.plus(1) ?: 0 // likeCount++

                binding.logLikeButton.setImageResource(R.drawable.baseline_favorite_24) // 채워진 하트 이미지로 변경
            }

            // 댓글 버튼
            binding.logCommentButton.setOnClickListener {// 댓글 버튼 클릭 시
                onLogSelectedListener?.onLogSelected(log.id!!) // CommentLogFragment로 이동
            }

        }

    }

    private inner class LogAdapter(private val logList: List<Log>) :
        RecyclerView.Adapter<LogHolder>() {
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

    private fun setupAppBarMenu() {
        val menuHost: MenuHost = requireActivity() // 메뉴를 관리하는 호스트
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
    }

    private fun fetchLogList() {
        database.child(Key.DB_LOGS).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) { // 메인 스레드에서 실행
                val logList = mutableListOf<Log>()

                snapshot.children.forEach {
                    val log = it.getValue(Log::class.java)
                    log ?: return

                    logList.add(log)
                }

                logList.sortByDescending { it.timeStamp } // 생성 시간 기준 정렬

                binding.logRecyclerView.adapter = LogAdapter(logList)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    companion object {
        fun newInstance(): LogFragment {
            return LogFragment()
        }
    }



}