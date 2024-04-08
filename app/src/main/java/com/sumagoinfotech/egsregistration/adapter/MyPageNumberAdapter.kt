package com.sumagoinfotech.egsregistration.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.egsregistration.MainActivity
import com.sumagoinfotech.egsregistration.R

public class MyPageNumberAdapter(var pageSize:Int, private val listener: OnPageNumberClickListener): RecyclerView.Adapter<MyPageNumberAdapter.ViewHolder>() {

    private var selectedPage = 1 // Default selected page is 1

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
        }
        /*if (position + 1 == selectedPage) {
            holder.tvPageNo.setTextColor(holder.itemView.context.resources.getColor(R.color.appBlue)) // Change text color or apply any other styling
        } else {
            holder.tvPageNo.setTextColor(holder.itemView.context.resources.getColor(R.color.black))
        }*/
    }
    interface OnPageNumberClickListener {
        fun onPageNumberClicked(pageNumber: Int)
    }
}