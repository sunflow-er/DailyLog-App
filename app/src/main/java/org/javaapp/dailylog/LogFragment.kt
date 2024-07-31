package org.javaapp.dailylog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.javaapp.dailylog.databinding.FragmentLogBinding


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

    companion object {
        fun newInstance() : LogFragment {
            return LogFragment()
        }
    }
}