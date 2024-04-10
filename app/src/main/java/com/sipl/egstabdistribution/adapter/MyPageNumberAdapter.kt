package com.sipl.egstabdistribution.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sipl.egstabdistribution.R

public class MyPageNumberAdapter(var pageSize:Int,var currentPage:String,private val listener: OnPageNumberClickListener): RecyclerView.Adapter<MyPageNumberAdapter.ViewHolder>() {

    private var selectedPage = 1 // Default selected page is 1
    private var selectedItemIndex: Int = 0

    fun  setSelectedPage(pageNumber: Int) {
        selectedPage = pageNumber
        notifyDataSetChanged() // Refresh the RecyclerView to update the UI
    }

    class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {

        val tvPageNo=itemView.findViewById<TextView>(R.id.tvPageNo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.item_row_pagination,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return pageSize;
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvPageNo.text=(position+1).toString()

        holder.itemView.setOnClickListener {
            listener.onPageNumberClicked((position+1))
            val previousSelectedItemIndex = selectedItemIndex
            selectedItemIndex = holder.adapterPosition
            notifyItemChanged(previousSelectedItemIndex)
            notifyItemChanged(selectedItemIndex)
        }
        if (Integer.parseInt(currentPage) == position+1) {
            //holder.tvPageNo.setTextColor(holder.itemView.context.resources.getColor(R.color.appBlue)) // Change to your desired color
            holder.tvPageNo.setTextColor(Color.RED) // Change to your desired color
        } else {
            holder.tvPageNo.setTextColor(Color.BLACK) // Change to your default text color
        }
    }
    interface OnPageNumberClickListener {
        fun onPageNumberClicked(pageNumber: Int)
    }
}