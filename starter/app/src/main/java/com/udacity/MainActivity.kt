package com.udacity

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    private var selectedUrl: DownloadURL? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        download_options_radio_group.setOnCheckedChangeListener { _, option ->
            when (option) {
                R.id.glide_download_button -> {
                    selectedUrl = DownloadURL.GLIDE_URL
                }
                R.id.load_app_download_button -> {
                    selectedUrl = DownloadURL.LOAD_APP_URL
                }
                R.id.retrofit_download_button -> {
                    selectedUrl = DownloadURL.RETROFIT_URL
                }
                else -> {}
            }
        }

        custom_button.setOnClickListener {
            if (selectedUrl == null) {
                Toast.makeText(applicationContext, getString(R.string.select_option_message), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PermissionInfo.PROTECTION_DANGEROUS)
                return@setOnClickListener
            }
            custom_button.buttonClicked()
            download()
        }
    }

    private fun buildNotification() {
        notificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java) as NotificationManager
        val intent = Intent(this, DetailActivity::class.java)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        }
    }

    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(selectedUrl?.url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    companion object {
        private enum class DownloadURL(val url: String?, val downloadMessage: String?) {
            GLIDE_URL("https://github.com/bumptech/glide", null),
            LOAD_APP_URL("https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter", null),
            RETROFIT_URL("https://github.com/square/retrofit", null)
        }

        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
    }

}
