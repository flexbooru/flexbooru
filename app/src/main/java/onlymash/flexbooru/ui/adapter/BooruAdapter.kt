package onlymash.flexbooru.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.model.Booru
import onlymash.flexbooru.ui.viewholder.BooruViewHolder

class BooruAdapter : RecyclerView.Adapter<BooruViewHolder>() {

    private var boorus: MutableList<Booru> = mutableListOf()

    init {
        setHasStableIds(true)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BooruViewHolder =
        BooruViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booru, parent, false))

    override fun getItemCount(): Int = boorus.size

    override fun onBindViewHolder(holder: BooruViewHolder, position: Int) {
        holder.bind(booru = boorus[position])
    }
    fun updateData(boorus: MutableList<Booru>) {
        this.boorus = boorus
        notifyDataSetChanged()
    }
}