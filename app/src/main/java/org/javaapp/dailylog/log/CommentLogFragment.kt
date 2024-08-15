package org.javaapp.dailylog.log

import android.os.Bundle
import android.renderscript.Sampler.Value
import android.view.Gravity
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
import org.javaapp.dailylog.UserNameCallback
import org.javaapp.dailylog.databinding.FragmentCommentLogBinding
import org.javaapp.dailylog.databinding.FragmentLogBinding
import org.javaapp.dailylog.databinding.ItemCommentBinding
import org.javaapp.dailylog.formatText
import org.javaapp.dailylog.getUserName
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID


class CommentLogFragment(private val logId : String?) : Fragment() {
    private lateinit var binding : FragmentCommentLogBinding
    private lateinit var currentUser : FirebaseUser
    private lateinit var database : DatabaseReference

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

        binding.commentRecyclerView.apply {
            layoutManager = NonScrollableLinearLayoutManager(context)
            adapter = CommentAdapter(emptyList())
        }
        
        // 댓글 입력 버튼 리스너 설정
        binding.commentSendButton.setOnClickListener {

            val commentId = UUID.randomUUID().toString() // 현재 시간(나노초)을 기준으로 고유 아이디값 생성
            val timeStamp = System.currentTimeMillis().toString() // 타임스탬프

            val comment = mutableMapOf<String, Any>()
            comment["id"] = commentId
            comment["userId"] = currentUser.uid
            comment["comment"] = formatText(binding.commentTypeEdit.text.toString())
            comment["timeStamp"] = timeStamp

            database.child(Key.DB_COMMENTS).child(logId!!).child(commentId).setValue(comment)

            binding.commentTypeEdit.text.clear()
        }

        // 로그 정보 가져오기
        database.child(Key.DB_LOGS).child(logId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val log = snapshot.getValue(Log::class.java)
                bindLog(log!!)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
        
        // 댓글 정보 가져오기
        database.child(Key.DB_COMMENTS).child(logId).orderByChild("timeStamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val commentList = mutableListOf<Comment>()

                snapshot.children.forEach {
                    val comment = it.getValue(Comment::class.java)
                    comment ?: return

                    commentList.add(comment)
                }

                binding.commentRecyclerView.adapter = CommentAdapter(commentList)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private inner class CommentHolder(private val binding : ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment : Comment) { // 내가 쓴 댓글일 때
            if (comment.userId == currentUser.uid) {
                binding.commentUserProfileImage.isVisible = false // 프로필 이미지 화면에서 지움
                binding.commentUserNameText.isVisible = false // 이름 화면에서 지움
                binding.commentContentText.apply {
                    text = comment.comment
                    gravity = Gravity.END
                }
                binding.commentLinearLayout.gravity = Gravity.END // 오른쪽 배치
            } else { // 다른 사람이 쓴 댓글일 때
                binding.commentUserProfileImage.apply {
                    isVisible = true
                    setImageResource(R.drawable.baseline_account_box_24)
                }
                binding.commentUserNameText.apply {
                    isVisible = true
                    binding.commentUserNameText.text = comment.userId
                }
                binding.commentContentText.apply {
                    text = comment.comment
                    gravity = Gravity.START
                }
                binding.commentLinearLayout.gravity = Gravity.START // 왼쪽 배치
            }

        }
    }

    private inner class CommentAdapter(private val commentList : List<Comment>) : RecyclerView.Adapter<CommentHolder>() {
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

    private fun bindLog(log : Log) {
        binding.commentUserProfileImage.setImageResource(R.drawable.baseline_account_box_24)
        getUserName(database, log.userId!!, object : UserNameCallback {
            override fun onUserNameRetrieved(userName: String?) {
                binding.commentUserNameText.text = userName
            }
        })
        binding.commentDateText.text = log.date
        binding.commentTimeText.text = log.time
        binding.commentContentImage.setImageResource(R.drawable.baseline_home_filled_100)
        binding.commentContentText.text = log.text
    }



}