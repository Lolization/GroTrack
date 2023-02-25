package com.termi.grotrack

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ShoppingAdapter(private val mList: List<Grocery>) :
    RecyclerView.Adapter<ShoppingAdapter.ViewHolder>() {

    private var onItemClick: ((Grocery) -> Unit)? = null
    private var onItemRemoveClick: ((Grocery) -> Unit)? = null

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.shopping_list_design, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val grocery = mList[position]

        // sets the text to the textview from our itemHolder class
        holder.tvName.text = grocery.name
        holder.tvCount.text = grocery.count.toString()
    }

    fun setOnItemClickListener(listener: (Grocery) -> Unit) {
        onItemClick = listener
    }

    fun setOnItemRemoveClickListener(listener: (Grocery) -> Unit) {
        onItemRemoveClick = listener
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    inner class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val tvCount: TextView = itemView.findViewById(R.id.tv_count)

        private val ibRemoveItem: ImageButton = itemView.findViewById(R.id.ib_remove)

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(mList[adapterPosition])
            }

            ibRemoveItem.setOnClickListener {
                onItemRemoveClick?.invoke(mList[adapterPosition])
            }
        }
    }
}