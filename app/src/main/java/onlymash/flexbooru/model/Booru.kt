package onlymash.flexbooru.model

import android.net.Uri
import android.util.Base64
import androidx.core.net.toUri
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "boorus", indices = [(Index(value = ["scheme", "host"], unique = true))])
data class Booru(
    @PrimaryKey(autoGenerate = true)
    var uid: Long,
    var name: String,
    var scheme: String,
    var host: String,
    var hash_salt: String?,
    // 0: danbooru 1: moebooru
    var type: Int
) {
    override fun toString(): String = toUri().toString()

    private fun toUri(): Uri {
        return Uri.Builder()
            .scheme("booru")
            .encodedAuthority(String.format(Locale.ENGLISH, "%s",
                Base64.encodeToString("$name@$scheme@$host@$type@${hash_salt ?: ""}".toByteArray(),
                    Base64.NO_PADDING or Base64.NO_WRAP or Base64.URL_SAFE)))
            .build()
    }

    companion object {
        fun url2Booru(str: String): Booru? {
            return try {
                val dataByte = Base64.decode(str.toUri().authority,
                    Base64.NO_PADDING or Base64.NO_WRAP or Base64.URL_SAFE)
                val dataList = String(dataByte).split("@")
                if (dataList.size == 5) {
                    Booru(uid = -1, name = dataList[0], scheme = dataList[1],
                        host = dataList[2], type = dataList[3].toInt(), hash_salt = dataList[4])
                } else {
                    null
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                null
            }
        }
    }
}