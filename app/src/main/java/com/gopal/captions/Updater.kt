package com.gopal.captions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object Updater {

    data class Info(val code: Int, val url: String, val notes: String)

    /** नया वर्ज़न हो तो Info देता है, वरना null. (बैकग्राउंड थ्रेड में चलाओ) */
    fun check(): Info? {
        val api = "https://api.github.com/repos/${BuildConfig.UPDATE_REPO}/releases/latest"
        val c = URL(api).openConnection() as HttpURLConnection
        c.setRequestProperty("Accept", "application/vnd.github+json")
        c.connectTimeout = 10000
        c.readTimeout = 10000
        if (c.responseCode != 200) return null
        val j = JSONObject(c.inputStream.bufferedReader().readText())
        val code = j.getString("tag_name").removePrefix("v").toIntOrNull() ?: return null
        val assets = j.getJSONArray("assets")
        var apk: String? = null
        for (i in 0 until assets.length()) {
            val a = assets.getJSONObject(i)
            if (a.getString("name").endsWith(".apk")) {
                apk = a.getString("browser_download_url"); break
            }
        }
        if (apk == null || code <= BuildConfig.VERSION_CODE) return null
        return Info(code, apk, j.optString("body", ""))
    }

    /** APK डाउनलोड करता है (बैकग्राउंड थ्रेड में चलाओ) */
    fun download(ctx: Context, url: String, onPercent: (Int) -> Unit): File {
        val dir = File(ctx.cacheDir, "update").apply { mkdirs() }
        val out = File(dir, "update.apk")
        val c = URL(url).openConnection() as HttpURLConnection
        c.instanceFollowRedirects = true
        c.connectTimeout = 15000
        c.readTimeout = 30000
        val total = c.contentLength.toLong()
        c.inputStream.use { input ->
            out.outputStream().use { o ->
                val buf = ByteArray(32 * 1024)
                var done = 0L
                while (true) {
                    val n = input.read(buf)
                    if (n < 0) break
                    o.write(buf, 0, n)
                    done += n
                    if (total > 0) onPercent((done * 100 / total).toInt())
                }
            }
        }
        return out
    }

    /** इंस्टॉल स्क्रीन खोलता है. पहली बार "अज्ञात ऐप्स इंस्टॉल" की इजाज़त माँगेगा */
    fun install(ctx: Context, apk: File) {
        if (!ctx.packageManager.canRequestPackageInstalls()) {
            ctx.startActivity(
                Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${ctx.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            return
        }
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", apk)
        ctx.startActivity(
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
