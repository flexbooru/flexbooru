package onlymash.flexbooru.receiver

import android.content.*
import onlymash.flexbooru.R
import onlymash.flexbooru.util.DownloadUtil
import onlymash.flexbooru.util.safeStringToUri

class DownloadNotificationClickReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val path = intent.getStringExtra(context.applicationContext.packageName + DownloadUtil.EXT_DOWNLOADED)
            if (path == null || !path.startsWith(ContentResolver.SCHEME_CONTENT)) return
            val newIntent = Intent().apply {
                action = Intent.ACTION_VIEW
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                setDataAndType(path.safeStringToUri(), "image/*")
            }
            try {
                context.startActivity(
                    Intent.createChooser(
                        newIntent,
                        context.getString(R.string.share_via)
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (_: ActivityNotFoundException) {}
        }
    }
}