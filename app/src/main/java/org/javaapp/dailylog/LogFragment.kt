package org.javaapp.dailylog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.javaapp.dailylog.databinding.FragmentLogBinding
import org.javaapp.dailylog.databinding.ItemLogPostBinding


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
        fun bind() {
            
        }
    }

    private inner class PostAdpater(private val logList : List<Post>) : RecyclerView.Adapter<PostHolder>() {
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