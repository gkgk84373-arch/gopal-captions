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
            text = "Gopal Captions"; textSize = 26f; gravity = Gravity.CENTER
        })
        status = TextView(this).apply {
            text = "वर्ज़न ${BuildConfig.VERSION_NAME}"; gravity = Gravity.CENTER; setPadding(0, 24, 0, 24)
        }
        root.addView(status)

        // यहीं आगे हम "वीडियो चुनो → ऑटो कैप्शन" वाला बटन जोड़ेंगे
        root.addView(Button(this).apply {
            text = "वीडियो चुनें (जल्द आ रहा है)"; isEnabled = false
        })

        updateBtn = Button(this).apply {
            text = "अपडेट जाँचें"
            setOnClickListener { onUpdateClick() }
        }
        root.addView(updateBtn)
        setContentView(root)

        checkForUpdate() // ऐप खुलते ही अपने आप जाँच
    }

    private fun checkForUpdate() {
        status.text = "अपडेट जाँच रहे हैं…"
        thread {
            val info = try { Updater.check() } catch (e: Exception) { null }
            runOnUiThread {
                pending = info
                if (info != null) {
                    status.text = "नया वर्ज़न मिला (${info.code})\n${info.notes}"
                    updateBtn.text = "अभी अपडेट करें"
                } else {
                    status.text = "वर्ज़न ${BuildConfig.VERSION_NAME} — ऐप नया है"
                    updateBtn.text = "अपडेट जाँचें"
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
                    runOnUiThread { status.text = "डाउनलोड हो रहा है… $p%" }
                }
                runOnUiThread {
                    updateBtn.isEnabled = true
                    status.text = "डाउनलोड हो गया, इंस्टॉल करें"
                    Updater.install(this, f)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    updateBtn.isEnabled = true
                    status.text = "डाउनलोड नहीं हुआ, इंटरनेट देखें"
                }
            }
        }
    }
}
