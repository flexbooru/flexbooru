package onlymash.flexbooru.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import onlymash.flexbooru.entity.ArtistUrlDan

class Converters {

    @TypeConverter
    fun fromStringToStringList(value: String): MutableList<String>? {
        val listType = object : TypeToken<MutableList<String>>(){}.type
        return Gson().fromJson<MutableList<String>>(value, listType)
    }

    @TypeConverter
    fun fromStringListToString(list: MutableList<String>): String =
        Gson().toJson(list)

    @TypeConverter
    fun fromStringToUrlDanList(value: String): MutableList<ArtistUrlDan>? {
        val listType = object : TypeToken<MutableList<ArtistUrlDan>>(){}.type
        return Gson().fromJson<MutableList<ArtistUrlDan>>(value, listType)
    }

    @TypeConverter
    fun fromUrlDanListToString(list: MutableList<ArtistUrlDan>): String =
            Gson().toJson(list)
}