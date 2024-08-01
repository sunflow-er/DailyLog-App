package org.javaapp.dailylog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.javaapp.dailylog.databinding.FragmentMyBinding

class MyFragment : Fragment() {
    private lateinit var binding : FragmentMyBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 임시 로그아웃 버튼 리스너 설정
        binding.signOutButton.setOnClickListener {
            Firebase.auth.signOut() // 로그아웃

            // 로그인 화면으로 이동
            val intent = Intent(requireActivity(), SignInActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }


    companion object {
        fun newInstance() : MyFragment {
            return MyFragment()
        }
    }
}