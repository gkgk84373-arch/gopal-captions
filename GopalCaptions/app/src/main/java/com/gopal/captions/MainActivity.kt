package com.gopal.captions

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var updateBtn: Button
    private var pending: Updater.Info? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }
        root.addView(TextView(this).apply {
            text = getString(R.string.app_name); textSize = 26f; gravity = Gravity.CENTER
        })
        status = TextView(this).apply {
            text = getString(R.string.version, BuildConfig.VERSION_NAME)
            gravity = Gravity.CENTER; setPadding(0, 24, 0, 24)
        }
        root.addView(status)

        // यहीं आगे "वीडियो चुनो → ऑटो कैप्शन" वाला बटन जुड़ेगा
        root.addView(Button(this).apply {
            text = getString(R.string.btn_pick_video); isEnabled = false
        })

        updateBtn = Button(this).apply {
            text = getString(R.string.btn_check)
            setOnClickListener { onUpdateClick() }
        }
        root.addView(updateBtn)
        setContentView(root)

        checkForUpdate()
    }

    private fun checkForUpdate() {
        status.text = getString(R.string.checking)
        thread {
            val info = try { Updater.check() } catch (e: Exception) { null }
            runOnUiThread {
                pending = info
                if (info != null) {
                    status.text = getString(R.string.new_version, info.code) + "\n" + info.notes
                    updateBtn.text = getString(R.string.btn_update)
                } else {
                    status.text = getString(R.string.up_to_date, BuildConfig.VERSION_NAME)
                    updateBtn.text = getString(R.string.btn_check)
                }
            }
        }
    }

    private fun onUpdateClick() {
        val info = pending ?: return checkForUpdate()
        updateBtn.isEnabled = false
        thread {
            try {
                val f = Updater.download(this, info.url) { p ->
                    runOnUiThread { status.text = getString(R.string.downloading, p) }
                }
                runOnUiThread {
                    updateBtn.isEnabled = true
                    status.text = getString(R.string.downloaded)
                    Updater.install(this, f)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    updateBtn.isEnabled = true
                    status.text = getString(R.string.download_failed)
                }
            }
        }
    }
}
