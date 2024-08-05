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
import org.javaapp.dailylog.R
import org.javaapp.dailylog.databinding.FragmentLogBinding
import org.javaapp.dailylog.databinding.ItemLogBinding
import java.text.SimpleDateFormat
import java.util.Locale


class LogFragment : Fragment() {

    // 프래그먼트에서 이벤트를 전달하기 위한 리스너 인터페이스 정의
    interface OnPostSelectedListener { // 게시글이 선택되었을 때
        fun onPostSelected()
    }
    interface OnAddSelectedListener { // 앱바 메뉴의 게시
        // 글 추가 버튼이 선택되었을 때
        fun onAddSelected()
    }

    private lateinit var binding : FragmentLogBinding
    private var onPostSelectedListener : OnPostSelectedListener? = null
    private var onAddSelectedListener : OnAddSelectedListener? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)

        onAddSelectedListener = context as OnAddSelectedListener
        onPostSelectedListener = context as OnPostSelectedListener
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

        // 앱바 메뉴 리스너 설정
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

        binding.logRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = PostAdpater(emptyList())
        }
    }

    override fun onDetach() {
        super.onDetach()
        onAddSelectedListener = null
        onPostSelectedListener = null
    }

    private inner class PostHolder(private val binding : ItemLogBinding) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("yyyy.MM.dd (E)", Locale.getDefault()) // 날짜 포맷
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()) // 시간 포맷


        fun bind(log : Log) {

            binding.logUserProfileImage.setImageResource(log.user?.profileImage ?: R.drawable.baseline_account_box_24) // 프로필 이미지
            binding.logUserNameText.setText(log.user?.name ?: "알 수 없음") // 사용자 이름
            binding.logDateText.setText(dateFormat.format(log.date)) // 게시 날짜
            binding.logTimeText.setText(timeFormat.format(log.date)) // 게시 시간
            if (log.image == null) { // 사진을 첨부하지 않았으면
                binding.logContentImage.isVisible = false // 보이지 않게
            } else { // 첨부했으면
                binding.logContentImage.apply {
                    setImageResource(log.image) // 보이게
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

    private inner class PostAdpater(private val logList : List<Log>) : RecyclerView.Adapter<PostHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
            val binding = ItemLogBinding.inflate(layoutInflater, parent, false)

            return PostHolder(binding)
        }

        override fun getItemCount(): Int {
            return logList.size
        }

        override fun onBindViewHolder(holder: PostHolder, position: Int) {
            holder.bind(logList[position])
        }

    }

    companion object {
        fun newInstance() : LogFragment {
            return LogFragment()
        }
    }
}