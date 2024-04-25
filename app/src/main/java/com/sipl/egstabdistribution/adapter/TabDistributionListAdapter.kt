package com.sipl.egstabdistribution.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sipl.egstabdistribution.R
import com.sipl.egstabdistribution.model.TabDistList.TabUser

class TabDistributionListAdapter(var tabUserList: MutableList<TabUser>,var currentPage:Int,var pageSize:Int,var onBeneficiaryClickListener: OnBeneficiaryClickListener) : RecyclerView.Adapter<TabDistributionListAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
        val tvFullName=itemView.findViewById<TextView>(R.id.tvFullName)
        val tvSrNo=itemView.findViewById<TextView>(R.id.tvSrNo)
        val tvAddress=itemView.findViewById<TextView>(R.id.tvAddress)
        val tvMobile=itemView.findViewById<TextView>(R.id.tvMobile)
        val ivPhoto=itemView.findViewById<ImageView>(R.id.ivPhoto)
    }

    interface OnBeneficiaryClickListener{
        fun onClick(user:TabUser);
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TabDistributionListAdapter.ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.item_row_user_list,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: TabDistributionListAdapter.ViewHolder, position: Int) {
        try {

            holder.tvFullName.text = tabUserList[position]?.full_name ?: ""
            holder.tvSrNo.text = ((currentPage - 1) * pageSize + position + 1).toString()
            holder.tvMobile.text = tabUserList[position]?.mobile_number ?: ""
            val address =
                "${tabUserList[position].district_name} ->${tabUserList[position].taluka_name} ->${tabUserList[position].village_name}"
            holder.tvAddress.text = address
            Glide.with(holder.itemView.context).load(tabUserList[position].photo_of_beneficiary)
                .override(90,75)
                .into(holder.ivPhoto)

           /* holder.itemView.setOnClickListener {
               *//* val intent= Intent(holder.itemView.context,BeneficiaryDetailsActivity::class.java)
                intent.putExtra("id",tabUserList[position].id.toString())
                Log.d("mytag",tabUserList[position].id.toString())
                holder.itemView.context.startActivity(intent)*//*
            }*/

            holder.itemView.setOnClickListener {
                onBeneficiaryClickListener.onClick(tabUserList[position])
            }
        }catch (e:Exception){

        }
    }

    override fun getItemCount(): Int {
        return tabUserList.size
    }
}