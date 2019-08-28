package com.github.rubensousa.recyclerviewsnap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(private val layoutId: Int = R.layout.adapter) :
    RecyclerView.Adapter<AppAdapter.VH>() {

    private var items = listOf<App>()

    fun setItems(list: List<App>) {
        this.items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    class VH(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val ratingTextView: TextView = itemView.findViewById(R.id.ratingTextView)

        init {
            view.setOnClickListener(this)
        }

        fun bind(app: App) {
            imageView.setImageResource(app.drawable)
            nameTextView.text = app.name
            ratingTextView.text = app.rating.toString()
        }

        override fun onClick(v: View?) {

        }
    }
}
