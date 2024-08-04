package org.javaapp.dailylog.log

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.javaapp.dailylog.databinding.FragmentAddPostBinding

class AddPostFragment : Fragment() {
    private lateinit var binding : FragmentAddPostBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddPostBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editImageButton.setOnClickListener {
            // TODO 이미지 불러오기
        }

        binding.postButton.setOnClickListener {
            // TODO 업로드 하기
        }
    }
}