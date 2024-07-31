package org.javaapp.dailylog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.javaapp.dailylog.databinding.FragmentWithBinding
import org.javaapp.dailylog.databinding.ItemWithUserBinding

class WithFragment : Fragment() {
    private lateinit var binding: FragmentWithBinding

    // dummy data
    val userList = listOf(
        User(R.drawable.baseline_account_box_24, "A", "A's status message"),
        User(R.drawable.baseline_account_box_24, "B", "B's status message"),
        User(R.drawable.baseline_account_box_24, "C", "C's status message"),
        User(R.drawable.baseline_account_box_24, "D", "D's status message"),
        User(R.drawable.baseline_account_box_24, "E", "E's status message")
    )

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
            adapter = UserAdapter(userList)
        }
    }

    private inner class UserHolder(private val binding: ItemWithUserBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user : User) {
            binding.userProfileImage.setImageResource(user.profileImage)
            binding.userNameText.text = user.name
            binding.userStatusMessageText.text = user.status_message
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