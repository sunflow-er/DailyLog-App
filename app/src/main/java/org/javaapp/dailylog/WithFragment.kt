package org.javaapp.dailylog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.javaapp.dailylog.databinding.FragmentWithBinding
import org.javaapp.dailylog.databinding.ItemWithUserBinding

class WithFragment : Fragment() {
    private lateinit var binding: FragmentWithBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWithBinding.inflate(inflater,container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.userRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = UserAdapter(emptyList())
        }

        // 파이어베이스 데이터베이스 사용자 리스트 정보 가져오기 (한 번만 가져오기)
        Firebase.database.reference.child(Key.DB_USERS).addListenerForSingleValueEvent( object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val userList = mutableListOf<User>()

                snapshot.children.forEach {
                    val user = it.getValue(User::class.java)
                    user ?: return

                    if(user.id != Firebase.auth.currentUser!!.uid) {
                        userList.add(user)
                    }
                }

                binding.userRecyclerView.adapter = UserAdapter(userList)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private inner class UserHolder(private val binding: ItemWithUserBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user : User) {
            binding.userProfileImage.setImageResource(user.profileImage ?: R.drawable.baseline_account_box_24)
            binding.userNameText.text = user.name
            binding.userStatusMessageText.text = user.statusMessage
        }
    }

    private inner class UserAdapter(private val userList : List<User>) : RecyclerView.Adapter<UserHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
            val binding = ItemWithUserBinding.inflate(layoutInflater, parent, false)

            return UserHolder(binding)
        }

        override fun getItemCount(): Int {
            return userList.size
        }

        override fun onBindViewHolder(holder: UserHolder, position: Int) {
            holder.bind(userList[position])
        }

    }

    companion object {
        fun newInstance() : WithFragment {
            return WithFragment()
        }
    }
}