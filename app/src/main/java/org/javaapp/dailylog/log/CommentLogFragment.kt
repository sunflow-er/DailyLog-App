package org.javaapp.dailylog.log

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        binding.commentRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CommentAdapter()
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