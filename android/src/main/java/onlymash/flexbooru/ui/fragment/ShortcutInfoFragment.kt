package onlymash.flexbooru.ui.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.Formatter
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import de.hdodenhof.circleimageview.CircleImageView
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Keys.POST_ID
import onlymash.flexbooru.common.Settings.activatedBooruUid
import onlymash.flexbooru.common.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.common.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.common.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.data.database.BooruManager
import onlymash.flexbooru.data.database.dao.PostDao
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.extension.copyText
import onlymash.flexbooru.extension.launchUrl
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.ui.activity.AccountActivity
import onlymash.flexbooru.ui.viewmodel.ShortcutViewModel
import onlymash.flexbooru.ui.viewmodel.getShortcutViewModel
import onlymash.flexbooru.widget.LinkTransformationMethod
import onlymash.flexbooru.worker.DownloadWorker
import org.kodein.di.erased.instance

class ShortcutInfoFragment : BaseBottomSheetDialogFragment() {

    companion object {
        fun create(postId: Int): ShortcutInfoFragment {
            return ShortcutInfoFragment().apply {
                arguments = Bundle().apply {
                    putInt(POST_ID, postId)
                }
            }
        }
    }

    private lateinit var behavior: BottomSheetBehavior<View>

    private val postDao by instance<PostDao>()

    private var postId = -1
    private var post: Post? = null

    private lateinit var booru: Booru

    private lateinit var shortcutViewModel: ShortcutViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            postId = getInt(POST_ID, -1)
        }
        val booru = BooruManager.getBooruByUid(activatedBooruUid)
        if (booru == null) {
            dismiss()
            return
        }
        this.booru = booru
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view= View.inflate(context, R.layout.fragment_bottom_sheet_info, null)
        dialog.setContentView(view)
        behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dismiss()
                }
            }

        })
        view.findViewById<Toolbar>(R.id.toolbar).apply {
            setTitle(R.string.browse_info_title)
            setNavigationOnClickListener {
                dismiss()
            }
        }
        val username = view.findViewById<AppCompatTextView>(R.id.user_name)
        val userId = view.findViewById<AppCompatTextView>(R.id.user_id)
        val date = view.findViewById<AppCompatTextView>(R.id.created_date)
        val source = view.findViewById<AppCompatTextView>(R.id.source_url).apply {
            transformationMethod = LinkTransformationMethod()
        }
        view.findViewById<View>(R.id.source_container).apply {
            setOnClickListener {
                source.text.let { text ->
                    context?.launchUrl(text.toString())
                }
            }
            setOnLongClickListener {
                source?.text?.let { text ->
                    context?.copyText(text)
                }
                true
            }
        }
        val rating = view.findViewById<AppCompatTextView>(R.id.rating)
        val score = view.findViewById<AppCompatTextView>(R.id.score)
        view.findViewById<AppCompatTextView>(R.id.url_sample_size).text = "not data"
        view.findViewById<AppCompatTextView>(R.id.url_larger_size).text = "not data"
        view.findViewById<LinearLayout>(R.id.url_sample_container).setOnLongClickListener {
            post?.let {
                context?.copyText(it.sample)
            }
            true
        }
        view.findViewById<LinearLayout>(R.id.url_larger_container).setOnLongClickListener {
            post?.let {
                context?.copyText(it.medium)
            }
            true
        }
        view.findViewById<LinearLayout>(R.id.url_origin_container).setOnLongClickListener {
            post?.let {
                context?.copyText(it.origin)
            }
            true
        }
        view.findViewById<ImageView>(R.id.url_origin_open).setOnClickListener {
            post?.let {
                openUrl(it.origin)
            }
        }
        view.findViewById<ImageView>(R.id.url_larger_open).setOnClickListener {
            post?.let {
                openUrl(it.medium)
            }
        }
        view.findViewById<ImageView>(R.id.url_sample_open).setOnClickListener {
            post?.let {
                openUrl(it.sample)
            }
        }
        view.findViewById<ImageView>(R.id.url_origin_download).setOnClickListener {
            post?.let {
                downloadUrl(it.origin)
            }
        }
        view.findViewById<ImageView>(R.id.url_larger_download).setOnClickListener {
            post?.let {
                downloadUrl(it.medium)
            }
        }
        view.findViewById<ImageView>(R.id.url_sample_download).setOnClickListener {
            post?.let {
                downloadUrl(it.sample)
            }
        }
        shortcutViewModel = getShortcutViewModel(postDao, booru.uid, postId)
        shortcutViewModel.post.observe(this, Observer { post ->
            this.post = post
            if (post != null) {
                username.text = post.uploader.name
                userId.text = post.uploader.id.toString()
                source.text = post.source
                view.findViewById<AppCompatTextView>(R.id.url_origin_size).text = getSize(post.width, post.height, post.size)
                if (booru.type != BOORU_TYPE_GEL) {
                    view.findViewById<ConstraintLayout>(R.id.user_container).setOnClickListener {
                        startActivity(Intent(requireContext(), AccountActivity::class.java).apply {
                            putExtra(AccountActivity.USER_ID_KEY, post.uploader.id)
                            putExtra(AccountActivity.USER_NAME_KEY, post.uploader.name)
                            putExtra(AccountActivity.USER_AVATAR_KEY, post.uploader.avatar)
                        })
                        dismiss()
                    }
                }
                if (booru.type == BOORU_TYPE_MOE && post.uploader.id > 0) {
                    GlideApp.with(this)
                        .load(String.format(getString(R.string.account_user_avatars), booru.scheme, booru.host, post.uploader.id))
                        .placeholder(ContextCompat.getDrawable(requireContext(), R.drawable.avatar_account))
                        .into(view.findViewById<CircleImageView>(R.id.user_avatar))
                } else if (booru.type == BOORU_TYPE_SANKAKU && !post.uploader.avatar.isNullOrBlank()) {
                    GlideApp.with(this)
                        .load(post.uploader.avatar)
                        .placeholder(ContextCompat.getDrawable(requireContext(), R.drawable.avatar_account))
                        .into(view.findViewById<CircleImageView>(R.id.user_avatar))
                }
                rating.text = when (post.rating) {
                    "s" -> getString(R.string.browse_info_rating_safe)
                    "q" -> getString(R.string.browse_info_rating_questionable)
                    else -> getString(R.string.browse_info_rating_explicit)
                }
                score.text = post.score.toString()
                date.text = post.date
            }
        })
        return dialog
    }

    private fun getSize(width: Int, height: Int, size: Int): String {
        return "$width x $height ${Formatter.formatFileSize(context, size.toLong())}"
    }

    private fun downloadUrl(url: String) {
        activity?.let {
            DownloadWorker.download(
                url = url,
                postId = postId,
                host = booru.host,
                activity = it
            )
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.parse(url)
        }
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {}
    }

    override fun onStart() {
        super.onStart()
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}