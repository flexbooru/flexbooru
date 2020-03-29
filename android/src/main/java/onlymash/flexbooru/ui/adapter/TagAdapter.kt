package onlymash.flexbooru.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.common.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.common.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.common.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.data.model.common.Tag
import onlymash.flexbooru.extension.copyText
import onlymash.flexbooru.ui.activity.SearchActivity

class TagAdapter(retryCallback: () -> Unit) : BasePagedListAdapter<Tag, RecyclerView.ViewHolder>(TAG_COMPARATOR, retryCallback) {
    companion object {
        val TAG_COMPARATOR = object : DiffUtil.ItemCallback<Tag>() {
            override fun areContentsTheSame(oldItem: Tag, newItem: Tag): Boolean =
                oldItem == newItem
            override fun areItemsTheSame(oldItem: Tag, newItem: Tag): Boolean =
                oldItem.id == newItem.id
        }
    }

    override fun onCreateItemViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        TagViewHolder.create(parent)

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val tag = getItem(position)
        (holder as TagViewHolder).apply {
            bind(tag)
            itemView.setOnClickListener {
                tag?.let { t ->
                    SearchActivity.startSearch(itemView.context, t.name)
                }
            }
            itemView.setOnLongClickListener {
                itemView.context.copyText(tag?.name)
                true
            }
        }
    }

    class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup): TagViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_tag, parent, false)
                return TagViewHolder(view)
            }
            const val GENERAL = 0
            const val ARTIST = 1
            const val COPYRIGHT = 3
            const val CHARACTER = 4
            const val CIRCLE = 5
            const val FAULTS = 6
            const val META = 5
            const val MODEL = 5
            const val PHOTO_SET = 6
            const val GENRE = 5
            const val MEDIUM = 8
            const val STUDIO = 2
            const val META_SANKAKU = 9
        }

        private val tagName: AppCompatTextView = itemView.findViewById(R.id.tag_name)
        private val tagType: AppCompatTextView = itemView.findViewById(R.id.tag_type)
        private val count: AppCompatTextView = itemView.findViewById(R.id.post_count)

        fun bind(data: Tag?) {
            val res = itemView.resources
            when (data?.booruType) {
                BOORU_TYPE_DAN -> {
                    tagName.text = data.name
                    tagType.text = when (data.category) {
                        GENERAL -> res.getString(R.string.tag_type_general)
                        ARTIST -> res.getString(R.string.tag_type_artist)
                        COPYRIGHT -> res.getString(R.string.tag_type_copyright)
                        CHARACTER -> res.getString(R.string.tag_type_character)
                        META -> res.getString(R.string.tag_type_meta)
                        else -> res.getString(R.string.tag_type_unknown)
                    }
                    count.text = data.count.toString()
                }
                BOORU_TYPE_MOE -> {
                    tagName.text = data.name
                    tagType.text = when (data.category) {
                        GENERAL -> res.getString(R.string.tag_type_general)
                        ARTIST -> res.getString(R.string.tag_type_artist)
                        COPYRIGHT -> res.getString(R.string.tag_type_copyright)
                        CHARACTER -> res.getString(R.string.tag_type_character)
                        CIRCLE -> res.getString(R.string.tag_type_circle)
                        FAULTS -> res.getString(R.string.tag_type_faults)
                        else -> res.getString(R.string.tag_type_unknown)
                    }
                    count.text = data.count.toString()
                }
                BOORU_TYPE_DAN1 -> {
                    tagName.text = data.name
                    tagType.text = when (data.category) {
                        GENERAL -> res.getString(R.string.tag_type_general)
                        ARTIST -> res.getString(R.string.tag_type_artist)
                        COPYRIGHT -> res.getString(R.string.tag_type_copyright)
                        CHARACTER -> res.getString(R.string.tag_type_character)
                        MODEL -> res.getString(R.string.tag_type_model)
                        PHOTO_SET -> res.getString(R.string.tag_type_photo_set)
                        else -> res.getString(R.string.tag_type_unknown)
                    }
                    count.text = data.count.toString()
                }
                BOORU_TYPE_GEL -> {
                    tagName.text = data.name
                    tagType.text = when (data.category) {
                        GENERAL -> res.getString(R.string.tag_type_general)
                        ARTIST -> res.getString(R.string.tag_type_artist)
                        COPYRIGHT -> res.getString(R.string.tag_type_copyright)
                        CHARACTER -> res.getString(R.string.tag_type_character)
                        META -> res.getString(R.string.tag_type_meta)
                        else -> res.getString(R.string.tag_type_unknown)
                    }
                    count.text = data.count.toString()
                }
                BOORU_TYPE_SANKAKU -> {
                    tagName.text = data.name
                    tagType.text = when (data.category) {
                        GENERAL -> res.getString(R.string.tag_type_general)
                        ARTIST -> res.getString(R.string.tag_type_artist)
                        COPYRIGHT -> res.getString(R.string.tag_type_copyright)
                        CHARACTER -> res.getString(R.string.tag_type_character)
                        MEDIUM -> res.getString(R.string.tag_type_medium)
                        META_SANKAKU -> res.getString(R.string.tag_type_meta)
                        STUDIO -> res.getString(R.string.tag_type_studio)
                        GENRE -> res.getString(R.string.tag_type_genre)
                        else -> res.getString(R.string.tag_type_unknown)
                    }
                    count.text = data.count.toString()
                }
            }
        }
    }
}