package org.javaapp.dailylog.log

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.javaapp.dailylog.R
import org.javaapp.dailylog.databinding.FragmentCommentLogBinding
import org.javaapp.dailylog.databinding.FragmentLogBinding
import org.javaapp.dailylog.databinding.ItemCommentBinding


class CommentLogFragment : Fragment() {
    private lateinit var binding : FragmentCommentLogBinding

    // dummy
    private lateinit var commentList : MutableList<Comment>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommentLogBinding.inflate(layoutInflater, container, false)

        // dummy
        commentList = mutableListOf<Comment>()
        commentList.add(Comment("1", "hello"))
        commentList.add(Comment("2", "hellohellohellohellohellohellohellohellohellohellohello"))


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

        binding.commentRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CommentAdapter(commentList)
        }
    }

    private inner class CommentHolder(private val binding : ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment : Comment) {
            binding.commentUserProfileImage.setImageResource(R.drawable.baseline_account_box_24)
            binding.commentUserNameText.text = comment.userId
            binding.commentContentText.text = comment.comment
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


}