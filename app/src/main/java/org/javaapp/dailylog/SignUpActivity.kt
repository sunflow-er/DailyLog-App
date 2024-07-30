package org.javaapp.dailylog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.javaapp.dailylog.databinding.ActivitySignInBinding
import org.javaapp.dailylog.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}