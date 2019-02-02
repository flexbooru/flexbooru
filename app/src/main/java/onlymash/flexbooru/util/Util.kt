package onlymash.flexbooru.util

import android.text.format.DateFormat
import java.util.*

fun formatDate(time: Long): CharSequence {
    val cal = Calendar.getInstance(Locale.getDefault())
    cal.timeInMillis = time
    return DateFormat.format("yyyy-MM-dd HH:mm", cal)
}
