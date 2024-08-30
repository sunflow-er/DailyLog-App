package org.javaapp.dailylog.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import org.javaapp.dailylog.databinding.FragmentUserBinding
import org.javaapp.dailylog.databinding.ItemUserBinding


class UserFragment : Fragment() {
    private lateinit var binding: FragmentUserBinding
    private lateinit var currentUser : FirebaseUser
    private lateinit var database : DatabaseReference

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
        binding = FragmentUserBinding.inflate(inflater,container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.userRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = UserAdapter(emptyList())
        }

        fetchUserList() // 파이어베이스 데이터베이스 사용자 리스트 정보 가져오기 (한 번만 가져오기)
    }

    private inner class UserHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user : User) {
            if (user.profileImage.isNullOrBlank()) {
                binding.userProfileImage.setImageResource(R.drawable.baseline_account_box_24)
            } else {
                Glide.with(requireContext()).load(user.profileImage).into(binding.userProfileImage)
            }
            binding.userNameText.text = user.name // 이름
            if (user.statusMessage.isNullOrBlank()) { // 상태메시지
                binding.userStatusMessageText.visibility = View.INVISIBLE
            } else {
                binding.userStatusMessageText.apply {
                    visibility = View.VISIBLE
                    text = user.statusMessage
                }
            }
        }
    }

    private inner class UserAdapter(private val userList : List<User>) : RecyclerView.Adapter<UserHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
            val binding = ItemUserBinding.inflate(layoutInflater, parent, false)

            return UserHolder(binding)
        }

        override fun getItemCount(): Int {
            return userList.size
        }

        override fun onBindViewHolder(holder: UserHolder, position: Int) {
            holder.bind(userList[position])
        }

    }

    private fun fetchUserList() {
        database.child(Key.DB_USERS).addListenerForSingleValueEvent( object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) { // 메인 스레드에서 실행
                val userList = mutableListOf<User>() // 화면에 보여줄 사용자를 담을 리스트

                snapshot.children.forEach {
                    val user = it.getValue(User::class.java)
                    user ?: return

                    if(user.id != currentUser.uid) { // 현재 사용자 계정이 아닌 경우에만
                        userList.add(user) // 화면에 보여준다.
                    }
                }

                binding.userRecyclerView.adapter = UserAdapter(userList)
            }

            override fun onCancelled(error: DatabaseError) {
                // TODO
            }
        })
    }

    companion object {
        fun newInstance() : UserFragment {
            return UserFragment()
        }
    }
}