package com.sipl.egstabdistribution.ui.activities

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sipl.egstabdistribution.R
import com.sipl.egstabdistribution.adapter.OfflineUserListAdapter
import com.sipl.egstabdistribution.database.AppDatabase
import com.sipl.egstabdistribution.database.dao.UserDao
import com.sipl.egstabdistribution.database.entity.User
import com.sipl.egstabdistribution.database.model.UsersWithAreaNames
import com.sipl.egstabdistribution.databinding.ActivityOfflineBeneficiaryDetailsBinding
import com.sipl.egstabdistribution.databinding.ActivitySyncUserBinding
import com.sipl.egstabdistribution.utils.CustomProgressDialog
import com.sipl.egstabdistribution.utils.NoInternetDialog
import io.getstream.photoview.PhotoView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OfflineBeneficiaryDetailsActivity : AppCompatActivity() {
    lateinit var binding: ActivityOfflineBeneficiaryDetailsBinding
    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    lateinit var userList:List<UsersWithAreaNames>
    lateinit var  adapter: OfflineUserListAdapter
    private lateinit var dialog: CustomProgressDialog

    private var isInternetAvailable=false
    private lateinit var noInternetDialog: NoInternetDialog
    private lateinit var user:UsersWithAreaNames
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityOfflineBeneficiaryDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title=resources.getString(R.string.beneficiary_details)
            database= AppDatabase.getDatabase(this)
            userDao=database.userDao()
            userList=ArrayList<UsersWithAreaNames>()
            var userId=intent.extras?.getString("id")
            CoroutineScope(Dispatchers.IO).launch {
                user= userDao.getUsersWithAreaNamesById(Integer.parseInt(userId))!!
                runOnUiThread {
                    initializeFields()
                }
            }
            binding.ivGramsevakId.setOnClickListener {
                showPhotoZoomDialog(user.gramsevakIdCardPhoto)
            }
            binding.ivPhoto.setOnClickListener {
                showPhotoZoomDialog(user.beneficaryPhoto)
            }
            binding.ivAadhar.setOnClickListener {
                showPhotoZoomDialog(user.aadharIdCardPhoto)
            }
            binding.ivImeiPhoto.setOnClickListener {
                showPhotoZoomDialog(user.tabletImeiPhoto)
            }
        } catch (e: Exception) {
            Log.d("mytag","Exception",e)
            e.printStackTrace()
        }
    }

    private fun initializeFields() {
        try {
            binding.tvFullName.text=user.fullName
            binding.tvDistritct.text=user.districtName
            binding.tvTaluka.text=user.talukaName
            if(user.village.equals("999999"))
            {
                binding.tvVillage.text=user.grampanchayatName
            }else{
                binding.tvVillage.text=user.villageName
            }

            binding.tvMobile.text=user.mobile
            binding.tvAadharCardNumber.text=user.aadharCardId
            loadImageWithRetry(binding.ivImeiPhoto,user.tabletImeiPhoto)
            loadImageWithRetry(binding.ivGramsevakId,user.gramsevakIdCardPhoto)
            loadImageWithRetry(binding.ivAadhar,user.aadharIdCardPhoto)
            loadImageWithRetry(binding.ivPhoto,user.beneficaryPhoto)
            if(user.isSyncFailed){
                binding.layoutSyncFailed.visibility= View.VISIBLE
                binding.tvFailedReason.text=user.syncFailedReason
            }else{
                binding.layoutSyncFailed.visibility= View.GONE
            }
            binding.tvLatLong.setText("${user.latitude},${user.longitude}")
        } catch (e: Exception) {
            Log.d("mytag","Exception",e)
            e.printStackTrace()
        }
    }
    fun loadImageWithRetry(imageView: ImageView, url: String, retryCount: Int = 3) {
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

                )
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        // Retry once

                        loadImageWithRetry(imageView,url,retryCount-1)
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
    private fun showPhotoZoomDialog(uri:String){

        try {
            val dialog= Dialog(this@OfflineBeneficiaryDetailsActivity)
            dialog.setContentView(R.layout.layout_zoom_image)
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setLayout(width, height)
            dialog.show()
            val photoView=dialog.findViewById<PhotoView>(R.id.photoView)
            val ivClose=dialog.findViewById<ImageView>(R.id.ivClose)
            Glide.with(this@OfflineBeneficiaryDetailsActivity)
                .load(uri)
                .into(photoView)

            ivClose.setOnClickListener {
                dialog.dismiss()
            }
        } catch (e: Exception) {
            Log.d("mytag","Exception",e)
            e.printStackTrace()
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}