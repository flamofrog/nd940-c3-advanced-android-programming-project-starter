package com.udacity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        prepareFragment()
    }

    private fun prepareFragment() {
        tv_filename_content.text = intent.getStringExtra(DOWNLOADED_FILE).toString()
        tv_status_content.apply {
            val status = intent.getStringExtra(DOWNLOAD_STATUS).toString()
            if (status == getString(R.string.download_success)) setTextColor(getColor(R.color.green))
            else setTextColor(getColor(R.color.red))
            text = status
        }
    }

}
