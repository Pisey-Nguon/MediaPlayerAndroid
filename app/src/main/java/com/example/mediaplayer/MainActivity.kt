package com.example.mediaplayer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mediaplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.btnProPlayerOnline.setOnClickListener {
            startActivity(Intent(this,ProPlayerOnlineActivity::class.java))
        }

        mBinding.btnBasicPlayerOnline.setOnClickListener {
            startActivity(Intent(this,BasicPlayerOnlineActivity::class.java))
        }
    }
}