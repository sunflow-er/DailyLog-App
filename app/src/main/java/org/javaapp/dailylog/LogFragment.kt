package org.javaapp.dailylog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.javaapp.dailylog.databinding.FragmentLogBinding
import org.javaapp.dailylog.databinding.ItemLogPostBinding
import java.text.SimpleDateFormat
import java.util.Locale


class LogFragment : Fragment() {
    private lateinit var binding : FragmentLogBinding

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

        binding.postRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = PostAdpater(emptyList())
        }
    }

    private inner class PostHolder(private val binding : ItemLogPostBinding) : RecyclerView.ViewHolder(binding.root) {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd (E)", Locale.getDefault()) // 날짜 포맷
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()) // 시간 포맷
        fun bind(post : Post) {
            binding.userProfileImage.setImageResource(post.user?.profileImage ?: R.drawable.baseline_account_box_24) // 프로필 이미지
            binding.userNameText.setText(post.user?.name ?: "알 수 없음") // 사용자 이름
            binding.postDateText.setText(dateFormat.format(post.date)) // 게시 날짜
            binding.postTimeText.setText(timeFormat.format(post.date)) // 게시 시간
            if (post.image == null) { // 사진을 첨부하지 않았으면
                binding.postImage.isVisible = false // 보이지 않게
            } else { // 첨부했으면
                binding.postImage.apply {
                    setImageResource(post.image) // 보이게
                    isVisible = true
                }
            }
            if (post.text.isNullOrBlank()) { // 글을 작성하지 않았으면
                binding.postText.isVisible = false // 보이지 않게
            } else { // 작성했다면
                binding.postText.apply {
                    setText(post.text) // 보이게
                    isVisible = true
                }
            }
        }
    }

    private inner class PostAdpater(private val postList : List<Post>) : RecyclerView.Adapter<PostHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
            val binding = ItemLogPostBinding.inflate(layoutInflater, parent, false)

            return PostHolder(binding)
        }

        override fun getItemCount(): Int {
            TODO("Not yet implemented")
        }

        override fun onBindViewHolder(holder: PostHolder, position: Int) {
            TODO("Not yet implemented")
        }

    }

    companion object {
        fun newInstance() : LogFragment {
            return LogFragment()
        }
    }
}