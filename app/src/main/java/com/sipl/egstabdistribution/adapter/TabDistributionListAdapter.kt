package com.sipl.egstabdistribution.adapter

import android.util.Log
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
           var address=""
            if(tabUserList[position].village_id.toString().equals("999999")){
                 address =
                    "${tabUserList[position].district_name} ->${tabUserList[position].taluka_name} ->${tabUserList[position].gram_panchayat_name}"
            }else{
                 address =
                    "${tabUserList[position].district_name} ->${tabUserList[position].taluka_name} ->${tabUserList[position].village_name}"
            }

            holder.tvAddress.text = address
            /*Glide.with(holder.itemView.context).load(tabUserList[position].photo_of_beneficiary)
                .override(90,75)
                .into(holder.ivPhoto)*/
            holder.itemView.setOnClickListener {
                onBeneficiaryClickListener.onClick(tabUserList[position])
            }
        }catch (e:Exception){
            Log.d("mytag","Exception",e)
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return tabUserList.size
    }
}