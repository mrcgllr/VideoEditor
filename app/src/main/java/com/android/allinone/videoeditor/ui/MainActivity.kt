package com.android.allinone.videoeditor.ui

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.allinone.videoeditor.R
import com.android.allinone.videoeditor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var closeAppDialog: Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initListener()
    }

    private fun initListener(){
        binding.apply {
            btnStart.setOnClickListener {
                startActivity(Intent(this@MainActivity, StartActivity::class.java))
            }

            ivShare.setOnClickListener {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name")
                var shareMessage = "\nLet me recommend you this application\n\n"
                shareMessage =
                    """
                ${shareMessage}https://play.google.com/store/apps/details?id=$packageName
                """.trimIndent()
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                startActivity(Intent.createChooser(shareIntent, "choose one"))
            }
            ivPrivacy.setOnClickListener {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse("https://androworld202.blogspot.com/")
                startActivity(i)
            }

            ivReta.setOnClickListener {
                val i3 = Intent(
                    Intent.ACTION_VIEW, Uri
                        .parse("market://details?id=$packageName")
                )
                startActivity(i3)
            }
        }
    }
}