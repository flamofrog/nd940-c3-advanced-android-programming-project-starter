package com.udacity

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

const val DOWNLOAD_STATUS = "downloadStatus"
const val DOWNLOADED_FILE = "downloadedFile"

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager

    private var selectedUrl: DownloadURL? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager

        createNotificationChannel()

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
                Toast.makeText(
                    applicationContext,
                    getString(R.string.select_option_message),
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PermissionInfo.PROTECTION_DANGEROUS
                )
                return@setOnClickListener
            }
            custom_button.buttonClicked()
            download()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_id),
                NotificationManager.IMPORTANCE_HIGH
            ).apply { setShowBadge(false) }
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.notification_channel_name)

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun sendNotification(filename: String, status: String) {
        val detailIntent = Intent(this, DetailActivity::class.java).apply {
            putExtra(DOWNLOAD_STATUS, status)
            putExtra(DOWNLOADED_FILE, filename)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            REQUEST_CODE,
            detailIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val action = NotificationCompat.Action.Builder(
            R.drawable.ic_launcher_foreground,
            getString(R.string.notification_button),
            pendingIntent
        ).build()

        val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_description))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(action)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_launcher_foreground)

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            var status: String? = null
            var filename: String? = null
            intent?.let {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                val downloadManager =
                    context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.query(DownloadManager.Query().apply {
                    setFilterById(
                        intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    )
                }).apply {
                    moveToFirst()
                    status =
                        if (getInt(getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) context.getString(
                            R.string.download_success
                        )
                        else context.getString(R.string.download_failure)
//                    filename = getString(getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))?.let {
//                        Uri.parse(it).path?.let { path -> File(path).absolutePath }
//                    }
                }
                filename = selectedUrl?.downloadMessage?.let { getString(it) }

                if (id == downloadID) {
                    custom_button.downloadComplete()
                    sendNotification(filename ?: "Unknown", status ?: "Unknown")
                }
            }
        }
    }

    private fun download() {

        val request = DownloadManager.Request(Uri.parse(selectedUrl?.url))
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
        private enum class DownloadURL(val url: String, val downloadMessage: Int) {
            GLIDE_URL("https://github.com/bumptech/glide", R.string.glide_dl_btn_text),
            LOAD_APP_URL(
                "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter",
                R.string.loadapp_dl_btn_text
            ),
            RETROFIT_URL("https://github.com/square/retrofit", R.string.retrofit_dl_btn_text)
        }

        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
        private const val NOTIFICATION_ID = 0
        private const val REQUEST_CODE = 0
    }

}
