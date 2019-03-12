package onlymash.flexbooru.database

import android.database.SQLException
import android.database.sqlite.SQLiteCantOpenDatabaseException
import androidx.lifecycle.LiveData
import com.crashlytics.android.Crashlytics
import onlymash.flexbooru.entity.Suggestion
import java.io.IOException

object SuggestionManager {

    @Throws(SQLException::class)
    fun createSuggestion(suggestion: Suggestion): Suggestion {
        suggestion.uid = 0L
        suggestion.uid = FlexbooruDatabase.suggestionDao.insert(suggestion)
        return suggestion
    }

    @Throws(SQLException::class)
    fun deleteSuggestion(uid: Long): Boolean = FlexbooruDatabase.suggestionDao.delete(uid) == 1

    @Throws(IOException::class)
    fun getSuggestionsByBooruUid(booruUid: Long): MutableList<Suggestion>? = try {
        FlexbooruDatabase.suggestionDao.getSuggestionsByBooruUid(booruUid)
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        Crashlytics.logException(ex)
        null
    }

    @Throws(IOException::class)
    fun getSuggestionsByBooruUidLiveData(booruUid: Long): LiveData<MutableList<Suggestion>>? = try {
        FlexbooruDatabase.suggestionDao.getSuggestionsByBooruUidLiveData(booruUid)
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        Crashlytics.logException(ex)
        null
    }
}