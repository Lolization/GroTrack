package com.termi.grotrack

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GroceryAdapter(private val mList: List<Grocery>) :
    RecyclerView.Adapter<GroceryAdapter.ViewHolder>() {
    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.groceries_list_design, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val grocery = mList[position]

        // sets the text to the textview from our itemHolder class
        holder.tvName.text = grocery.name
        holder.tvCount.text = grocery.count.toString()
        Log.d(Consts.TAG, "LOCATION: ${grocery.location}")
        holder.tvLocation.text = grocery.location

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val tvCount: TextView = itemView.findViewById(R.id.tv_count)
        val tvLocation: TextView = itemView.findViewById(R.id.tv_location)
    }
}