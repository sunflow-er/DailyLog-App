package org.javaapp.dailylog.log

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
import org.javaapp.dailylog.R
import org.javaapp.dailylog.UserInfoCallback
import org.javaapp.dailylog.databinding.FragmentCommentLogBinding
import org.javaapp.dailylog.databinding.ItemCommentBinding
import org.javaapp.dailylog.formatText
import org.javaapp.dailylog.getUserInfo
import org.javaapp.dailylog.user.User
import java.util.UUID


class CommentLogFragment(private val logId: String) : Fragment() {
    private lateinit var binding: FragmentCommentLogBinding
    private lateinit var currentUser: FirebaseUser
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentUser = Firebase.auth.currentUser!!
        database = Firebase.database.reference
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommentLogBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 앱바 메뉴 인플레이트 및 리스너 설정
        val menuHost: MenuHost = requireActivity()
        val menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_comment_log, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.modify_my_log -> {
                        // TODO 로그 내용 수정
                        true
                    }

                    R.id.delete_my_log -> {
                        database.child(Key.DB_LOGS).child(logId).removeValue() // 로그 삭제
                        database.child(Key.DB_COMMENTS).child(logId).removeValue() // 댓글 삭제

                        requireActivity().supportFragmentManager.popBackStack()  // 프래그먼트 종료
                        true
                    }

                    else -> false
                }
            }
        }
        menuHost.addMenuProvider(menuProvider, viewLifecycleOwner)

        // 댓글 리사이클러뷰 설정
        binding.commentLogRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CommentAdapter(emptyList())
        }

        // 로그 정보 가져오기
        database.child(Key.DB_LOGS).child(logId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isAdded) { // 프래그먼트가 현재 액티비티에 추가(연결)되었는지 확인, 프래그먼트가 활성 상태인지 확인
                    val log = snapshot.getValue(Log::class.java)

                    bindLog(log!!)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        // 댓글 정보 가져오기
        database.child(Key.DB_COMMENTS).child(logId).orderByChild("timeStamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (isAdded) { // 프래그먼트가 활성 상태인지 확인
                        val commentList = mutableListOf<Comment>()

                        snapshot.children.forEach {
                            val comment = it.getValue(Comment::class.java)
                            comment ?: return

                            commentList.add(comment)
                        }


                        binding.commentLogRecyclerView.adapter = CommentAdapter(commentList)

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        // 댓글 입력 버튼 리스너 설정
        binding.commentLogSendButton.setOnClickListener {

            val commentId = UUID.randomUUID().toString() // 현재 시간(나노초)을 기준으로 고유 아이디값 생성
            val timeStamp = System.currentTimeMillis().toString() // 타임스탬프

            val comment = mutableMapOf<String, Any>()
            comment["id"] = commentId
            comment["userId"] = currentUser.uid
            comment["comment"] = formatText(binding.commentLogTypeEdit.text.toString())
            comment["timeStamp"] = timeStamp

            database.child(Key.DB_COMMENTS).child(logId!!).child(commentId).setValue(comment)

            // EditText 내용 비우기
            binding.commentLogTypeEdit.text.clear()

            // 키보드 내리기
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(
                binding.commentLogTypeEdit.windowToken,
                0
            ) // commentTypeEdit 윈도우의 키보드를 숨긴다.

            // 키보드가 완전히 내려간 후 스크롤뷰 맨 아래로 이동
            binding.commentLogTypeEdit.postDelayed({
                binding.commentLogNestedScrollView.fullScroll(View.FOCUS_DOWN) // 주어진 방향으로 스크롤 뷰를 완전히 스크롤
            }, 500) // 500ms 지연
        }
    }

    private inner class CommentHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) { // 내가 쓴 댓글일 때
            if (comment.userId == currentUser.uid) {
                binding.commentUserProfileImage.visibility = View.INVISIBLE // 프로필 이미지 화면에서 지움
                binding.commentUserNameText.visibility = View.INVISIBLE // 이름 화면에서 지움
                binding.commentContentText.apply {
                    text = comment.comment
                    gravity = Gravity.END
                }
                binding.commentLinearLayout.gravity = Gravity.END // 오른쪽 배치
            } else { // 다른 사람이 쓴 댓글일 때
                // 댓글 작성자 이름, 프로필 이미지 가져오기
                getUserInfo(database, comment.userId!!, object : UserInfoCallback {
                    override fun onUserInfoRetrieved(userInfo: User) {
                        binding.commentUserNameText.apply {
                            visibility = View.VISIBLE
                            text = userInfo.name
                        }

                        binding.commentUserProfileImage.visibility = View.VISIBLE
                        Glide.with(requireContext()).load(userInfo.profileImage)
                            .into(binding.commentUserProfileImage)
                    }
                })
                binding.commentContentText.apply {
                    text = comment.comment
                    gravity = Gravity.START
                }
                binding.commentLinearLayout.gravity = Gravity.START // 왼쪽 배치
            }

        }
    }

    private inner class CommentAdapter(private val commentList: List<Comment>) :
        RecyclerView.Adapter<CommentHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
            val binding = ItemCommentBinding.inflate(layoutInflater, parent, false)

            return CommentHolder(binding)
        }

        override fun getItemCount(): Int {
            return commentList.size
        }

        override fun onBindViewHolder(holder: CommentHolder, position: Int) {
            holder.bind(commentList[position])
        }

    }

    private fun bindLog(log: Log) {
        // 로그 작성자 이름, 프로필 이미지 가져오기
        getUserInfo(database, log.userId!!, object : UserInfoCallback {
            override fun onUserInfoRetrieved(userInfo: User) {
                binding.commentLogUserNameText.text = userInfo.name
                Glide.with(requireContext()).load(userInfo.profileImage)
                    .into(binding.commentLogUserProfileImage)
            }
        })

        binding.commentLogDateText.text = log.date
        binding.commentLogTimeText.text = log.time

        if (log.image.isNullOrBlank()) { // 사진을 첨부하지 않았으면
            binding.commentLogContentImage.isVisible = false // 보이지 않게
        } else { // 첨부했으면
            binding.commentLogContentImage.isVisible = true

            // 이미지 업로드
            Glide.with(requireContext())
                .load(log.image.toUri())
                .into(binding.commentLogContentImage)
        }

        if (log.text.isNullOrBlank()) {
            binding.commentLogContentText.isVisible = false
        } else {
            binding.commentLogContentText.apply {
                isVisible = true
                text = log.text
            }
        }
    }


}