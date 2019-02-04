package onlymash.flexbooru.model

import android.content.res.Resources
import android.graphics.drawable.Drawable
import onlymash.flexbooru.R

data class Placeholder(
    var s: Drawable,
    var q: Drawable,
    var e: Drawable) {

    companion object {
        fun create(resources: Resources, theme: Resources.Theme): Placeholder {
            return Placeholder(
                s = resources.getDrawable(R.drawable.background_rating_s, theme),
                q = resources.getDrawable(R.drawable.background_rating_q, theme),
                e = resources.getDrawable(R.drawable.background_rating_e, theme))
        }
    }
}