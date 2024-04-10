package com.sumagoinfotech.egsregistration.ui

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import com.sumagoinfotech.egsregistration.R
import com.sumagoinfotech.egsregistration.databinding.ActivityBeneficiaryDetailsBinding
import com.sumagoinfotech.egsregistration.model.areamaster.MasterUpdateModel
import com.sumagoinfotech.egsregistration.model.delete.DeleteModel
import com.sumagoinfotech.egsregistration.model.detailsbyid.UserDetailsModel
import com.sumagoinfotech.egsregistration.utils.CustomProgressDialog
import com.sumagoinfotech.egsregistration.utils.MySharedPref
import com.sumagoinfotech.egsregistration.webservice.ApiClient
import io.getstream.photoview.PhotoView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BeneficiaryDetailsActivity : AppCompatActivity() {
    private lateinit var binding:ActivityBeneficiaryDetailsBinding
    private var gramsevakIdPhoto=""
    private var imeiPhoto=""
    private var photo=""
    private var aadharImage=""
    private lateinit var dialog: CustomProgressDialog
    private var beneficiaryid=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityBeneficiaryDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.beneficiary_details)
        dialog= CustomProgressDialog(this)
        beneficiaryid=intent.getStringExtra("id").toString()
        Log.d("mytag",beneficiaryid)
        getLabourDetails(beneficiaryid)
        binding.ivGramsevakId.setOnClickListener {
            showPhotoZoomDialog(gramsevakIdPhoto)
        }
        binding.ivPhoto.setOnClickListener {
            showPhotoZoomDialog(photo)
        }
        binding.ivAadhar.setOnClickListener {
            showPhotoZoomDialog(aadharImage)
        }
        binding.ivImeiPhoto.setOnClickListener {
            showPhotoZoomDialog(imeiPhoto)
        }


    }

    override fun onResume() {
        super.onResume()

    }
    private fun getLabourDetails(id:String) {

        try {
            dialog.show()
            val apiService= ApiClient.create(this@BeneficiaryDetailsActivity)
            apiService.getBeneficiaryById(id).enqueue(object :
                Callback<UserDetailsModel> {
                override fun onResponse(
                    call: Call<UserDetailsModel>,
                    response: Response<UserDetailsModel>
                ) {
                    dialog.dismiss()
                    if(response.isSuccessful){
                        if(!response.body()?.data.isNullOrEmpty()) {
                            val list=response.body()?.data
                            Log.d("mytag",""+ Gson().toJson(response.body()));
                            binding.tvFullName.text=list?.get(0)?.full_name
                            binding.tvDistritct.text=list?.get(0)?.district_name
                            binding.tvTaluka.text=list?.get(0)?.taluka_name
                            binding.tvVillage.text=list?.get(0)?.village_name
                            binding.tvMobile.text=list?.get(0)?.mobile_number
                            binding.tvAadharCardNumber.text=list?.get(0)?.adhar_card_number.toString()
                            binding.tvGrampanchayatName.text=list?.get(0)?.gram_panchayat_name
                            binding.tvLatLong.text=list?.get(0)?.latitude.toString()+","+list?.get(0)?.longitude.toString()
                            photo= list?.get(0)?.photo_of_beneficiary.toString()
                            imeiPhoto= list?.get(0)?.photo_of_tablet_imei.toString()
                            aadharImage= list?.get(0)?.aadhar_image.toString()
                            gramsevakIdPhoto= list?.get(0)?.gram_sevak_id_card_photo.toString()
                            /*Glide.with(this@BeneficiaryDetailsActivity).load(imeiPhoto).into(binding.ivImeiPhoto)
                            Glide.with(this@BeneficiaryDetailsActivity).load(photo).into(binding.ivPhoto)
                            Glide.with(this@BeneficiaryDetailsActivity).load(aadharImage).into(binding.ivAadhar)
                            Glide.with(this@BeneficiaryDetailsActivity).load(gramsevakIdPhoto).into(binding.ivGramsevakId)*/

                            loadImageWithRetry(binding.ivImeiPhoto,imeiPhoto)
                            loadImageWithRetry(binding.ivGramsevakId,gramsevakIdPhoto)
                            loadImageWithRetry(binding.ivAadhar,aadharImage)
                            loadImageWithRetry(binding.ivPhoto,photo)
                        }else {
                            Toast.makeText(this@BeneficiaryDetailsActivity, "No records found", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else{
                        Toast.makeText(this@BeneficiaryDetailsActivity, "Response unsuccessful", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<UserDetailsModel>, t: Throwable) {
                    Log.d("mytag","Exception "+t.message)
                    t.printStackTrace()
                    dialog.dismiss()
                    Toast.makeText(this@BeneficiaryDetailsActivity, "Error Ocuured during api call", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            dialog.dismiss()
            e.printStackTrace()
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            finish()
        }
        if(item.itemId==R.id.action_delete){
            AlertDialog.Builder(this)
                .setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_beneficiary_details))
                .setIcon(R.drawable.ic_delete)
                .setTitle(R.string.delete)
                .setPositiveButton("Yes") { dialog, _ ->
                    deleteBeneficiary(beneficiaryid)
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                    // Handle the case when the user chooses not to enable GPS
                }
                .show()

        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteBeneficiary(id:String) {
        try {
            dialog.show()
            val apiService= ApiClient.create(this@BeneficiaryDetailsActivity)
            apiService.deleteBeneficiaryById(id).enqueue(object :
                Callback<DeleteModel> {
                override fun onResponse(
                    call: Call<DeleteModel>,
                    response: Response<DeleteModel>
                ) {
                    dialog.dismiss()
                    if(response.isSuccessful){
                        if(!response.body()?.status.isNullOrEmpty()) {

                            if(response.body()?.status.equals("true")){
                                Toast.makeText(this@BeneficiaryDetailsActivity, response.body()?.message, Toast.LENGTH_SHORT)
                                    .show()
                                finish()
                            }else{
                                Toast.makeText(this@BeneficiaryDetailsActivity, response.body()?.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }else {
                            Toast.makeText(this@BeneficiaryDetailsActivity, response.body()?.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else{
                        Toast.makeText(this@BeneficiaryDetailsActivity,  response.body()?.message, Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<DeleteModel>, t: Throwable) {
                    Log.d("mytag","Exception "+t.message)
                    t.printStackTrace()
                    dialog.dismiss()
                    Toast.makeText(this@BeneficiaryDetailsActivity,
                        getString(R.string.error_occurred_during_api_call), Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            dialog.dismiss()
            e.printStackTrace()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete,menu)
        return true
    }

    private fun showPhotoZoomDialog(uri:String){

        try {
            val dialog= Dialog(this@BeneficiaryDetailsActivity)
            dialog.setContentView(R.layout.layout_zoom_image)
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setLayout(width, height)
            dialog.show()
            val photoView=dialog.findViewById<PhotoView>(R.id.photoView)
            val ivClose=dialog.findViewById<ImageView>(R.id.ivClose)
            Glide.with(this@BeneficiaryDetailsActivity)
                .load(uri)
                .into(photoView)
            ivClose.setOnClickListener {
                dialog.dismiss()
            }
        } catch (e: Exception) {

        }
    }
    fun loadImageWithRetry(imageView: ImageView, url: String) {
        try {
            Glide.with(imageView.context)
                .load(url)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.progress_bg) // Placeholder image while loading
                        .error(R.drawable.ic_error) // Image to display if loading fails
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache strategy
                        .skipMemoryCache(false) // Whether to skip the memory cache
                        .override(200,200) // Specify the size of the image
                        .centerCrop() // Crop type

                )
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        // Retry once
                        Glide.with(imageView.context)
                            .load(url)
                            .apply(
                                RequestOptions()
                                    .placeholder(R.drawable.progress_bg) // Placeholder image while loading
                                    .error(R.drawable.ic_error) // Image to display if loading fails
                                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache strategy
                                    .skipMemoryCache(false) // Whether to skip the memory cache
                                    .override(200,200) // Specify the size of the image
                                    .centerCrop() // Crop type
                            )
                            .into(imageView)
                        return false // Return false to let Glide handle the error
                    }
                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .into(imageView)
        } catch (e: Exception) {
            Log.d("mytag","Exception "+e.message)
            e.printStackTrace()
        }
    }
}