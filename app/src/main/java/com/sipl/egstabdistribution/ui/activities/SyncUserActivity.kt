package com.sipl.egstabdistribution.ui.activities

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.Formatter.formatFileSize
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.google.gson.Gson
import com.sipl.egstabdistribution.R
import com.sipl.egstabdistribution.adapter.OfflineUserListAdapter
import com.sipl.egstabdistribution.database.AppDatabase
import com.sipl.egstabdistribution.database.dao.UserDao
import com.sipl.egstabdistribution.database.entity.User
import com.sipl.egstabdistribution.database.model.UsersWithAreaNames
import com.sipl.egstabdistribution.databinding.ActivityHomeBinding
import com.sipl.egstabdistribution.databinding.ActivitySyncUserBinding
import com.sipl.egstabdistribution.interfaces.OnUserDeleteListener
import com.sipl.egstabdistribution.utils.CustomProgressDialog
import com.sipl.egstabdistribution.utils.NoInternetDialog
import com.sipl.egstabdistribution.webservice.ApiClient
import com.sipl.egstabdistribution.webservice.FileInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class SyncUserActivity : AppCompatActivity(),OnUserDeleteListener {
    lateinit var binding:ActivitySyncUserBinding
    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    lateinit var userList:List<UsersWithAreaNames>
    lateinit var  adapter: OfflineUserListAdapter
    private lateinit var dialog: CustomProgressDialog

    private var isInternetAvailable=false
    private lateinit var noInternetDialog: NoInternetDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySyncUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.sync_user_data)
        dialog=CustomProgressDialog(this)
        dialog.setCancelable(false)
        dialog.show()
        val layoutManager= LinearLayoutManager(this, RecyclerView.VERTICAL,false)
        binding.recyclerView.layoutManager=layoutManager
        database= AppDatabase.getDatabase(this)
        userDao=database.userDao()
        userList=ArrayList<UsersWithAreaNames>()
        adapter= OfflineUserListAdapter(userList,this)
        adapter.notifyDataSetChanged()

        noInternetDialog= NoInternetDialog(this)
        ReactiveNetwork
            .observeNetworkConnectivity(applicationContext)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ connectivity: Connectivity ->
                Log.d("##", "=>" + connectivity.state())
                if (connectivity.state().toString() == "CONNECTED") {
                    isInternetAvailable = true
                    noInternetDialog.hideDialog()
                } else {
                    isInternetAvailable = false
                    noInternetDialog.showDialog()
                }
            }) { throwable: Throwable? -> }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_sync,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            finish()
        }
        if(item.itemId==R.id.navigation_sync){

            if(isInternetAvailable){
                CoroutineScope(Dispatchers.IO).launch {
                   uploadUsersOnline()
                }
            }else{
                noInternetDialog.showDialog()
            }

        }

        return super.onOptionsItemSelected(item)
    }


        private suspend fun uploadUsersOnline(){

            val apiService = ApiClient.create(this@SyncUserActivity)
            CoroutineScope(Dispatchers.IO).launch {
                val users = getUsersFromDatabase()
                runOnUiThread {
                    dialog.show()
                }

                try {
                    users.forEach { userRecord ->
                        try {

                            val aadharCardImage =
                                createFilePart(FileInfo(fileUri = userRecord.aadharIdCardPhoto, fileName = "aadhar_image"))
                            val gramsevakIdImage =
                                createFilePart(FileInfo(fileUri = userRecord.gramsevakIdCardPhoto,fileName = "gram_sevak_id_card_photo"))
                            val profileImage =createFilePart(FileInfo(fileUri = userRecord.beneficaryPhoto,fileName = "photo_of_beneficiary"))
                            val tabletImeiImage =
                                createFilePart(FileInfo(fileUri = userRecord.tabletImeiPhoto,fileName = "photo_of_tablet_imei"))
                            val response= apiService.uploadLaborInfo(
                                fullName = userRecord.fullName,
                                grampanchayatName = userRecord.grampanchayatName!!,
                                aadharNumber = userRecord.aadharCardId,
                                districtId=userRecord.district,
                                talukaId =userRecord.taluka,
                                villageId = userRecord.village,
                                mobileNumber = userRecord.mobile,
                                latitude=userRecord.latitude,
                                longitude = userRecord.longitude,
                                aadharFile = aadharCardImage!!,
                                gramsevakIdFile = gramsevakIdImage!!,
                                photoFile = profileImage!!,
                                tabletImeiFile = tabletImeiImage!!,)
                            if(response.isSuccessful){
                                if(response.body()?.status.equals("True"))
                                {
                                    userRecord.isSynced=true
                                    userDao.updateUser(userRecord)
                                    val filesList= mutableListOf<Uri>()
                                    filesList.add(Uri.parse(userRecord.aadharIdCardPhoto))
                                    filesList.add(Uri.parse(userRecord.beneficaryPhoto))
                                    filesList.add(Uri.parse(userRecord.gramsevakIdCardPhoto))
                                    filesList.add(Uri.parse(userRecord.tabletImeiPhoto))
                                    deleteFilesFromFolder(filesList)
                                    userRecord.id?.let { userDao.deleteUserById(it) }
                                }else{
                                    userRecord.isSyncFailed=true
                                    userRecord.syncFailedReason=response.body()?.message
                                    userDao.updateUser(userRecord)
                                }
                            }else{
                                userRecord.isSyncFailed=true
                                if(response.body()?.message.isNullOrEmpty()){
                                    userRecord.syncFailedReason="Unsuccessful Response from api with code "+response.code()
                                }else{
                                    userRecord.syncFailedReason=response.body()?.message
                                }
                                userDao.updateUser(userRecord)
                                withContext(Dispatchers.Main){
                                }
                            }

                            fetchUserList()
                        } catch (e: Exception) {
                            Log.d("mytag","Exception : ${e.message}",e)
                            e.printStackTrace()
                            withContext(Dispatchers.Main){
                                Toast.makeText(this@SyncUserActivity,resources.getString(R.string.error_occurred_during_api_call),
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    runOnUiThread {dialog.dismiss()  }
                }catch (e:Exception){
                    Log.d("mytag","Exception : ${e.message}",e)
                    e.printStackTrace()
                    runOnUiThread { dialog.dismiss() }
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@SyncUserActivity,resources.getString(R.string.failed_updating_labour_response),
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    private suspend fun getUsersFromDatabase(): List<User> {
        val list = AppDatabase.getDatabase(applicationContext).userDao().getAllUsers()
        return list
    }
    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch{
            userList=userDao.getUsersWithAreaNames()
            Log.d("mytag","=>"+userList.size)
            val listWithName=userDao.getUsersWithAreaNames()
            if(!listWithName.isNullOrEmpty()){

                Log.d("mytag",""+ Gson().toJson(listWithName))
            }else{
                Log.d("mytag","Empty Or Nukk ")
            }
            withContext(Dispatchers.Main) {
                dialog.dismiss()
            adapter= OfflineUserListAdapter(userList,this@SyncUserActivity)
            binding.recyclerView.adapter=adapter
            adapter.notifyDataSetChanged() // Notify the adapter that the data has changed
            }
        }
        Log.d("mytag",""+userList.size)
    }

    fun fetchUserList(){
        CoroutineScope(Dispatchers.IO).launch{
            userList=userDao.getUsersWithAreaNames()
            withContext(Dispatchers.Main) {
                adapter= OfflineUserListAdapter(userList,this@SyncUserActivity)
                binding.recyclerView.adapter=adapter
                adapter.notifyDataSetChanged() // Notify the adapter that the data has changed
            }
        }
        Log.d("mytag",""+userList.size)
    }

    private suspend fun createFilePart(fileInfo: FileInfo): MultipartBody.Part? {
        Log.d("mytag",""+fileInfo.fileUri)
        val file: File? = uriToFile(applicationContext, fileInfo.fileUri)

        return file?.let {
            Log.d("mytag",""+formatFileSize(it))
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), it)
            MultipartBody.Part.createFormData(fileInfo.fileName, it.name, requestFile)
        }
    }
    suspend fun uriToFile(context: Context, uri: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val requestOptions = RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Don't cache to avoid reading from cache
                    .skipMemoryCache(true) // Skip memory cache
                val bitmap = Glide.with(context)
                    .asBitmap()
                    .load(uri)
                    .apply(requestOptions)
                    .submit()
                    .get()
                val time= Calendar.getInstance().timeInMillis.toString()
                // Create a temporary file to store the bitmap
                val file = File(context.cacheDir, "$time.jpg")
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()

                file // Return the temporary file
            } catch (e: Exception) {
                Log.d("mytag", "Exception uriToFile: ${e.message}")
                null // Return null if there's an error
            }
        }
    }
    private suspend fun deleteFilesFromFolder(urisToDelete: List<Uri>) {
        try {
            val mediaStorageDir = File(externalMediaDirs[0], "myfiles")
            val files = mediaStorageDir.listFiles()
            files?.forEach { file ->
                if (file.isFile) {
                    val fileUri = Uri.fromFile(file)
                    if (urisToDelete.contains(fileUri)) {
                        if (file.delete()) {
                            Log.d("mytag", "Deleted file: ${file.absolutePath}")
                        } else {
                            Log.d("mytag", "Failed to delete file: ${file.absolutePath}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("mytag", "Failed to delete file: ${e.message}")
        }
    }
    fun formatFileSize(file: File): String {
        val fileSizeInBytes = file.length()
        val fileSizeInKB = fileSizeInBytes / 1024
        val fileSizeInMB = fileSizeInKB / 1024

        return when {
            fileSizeInMB > 0 -> String.format("%.2f MB", fileSizeInMB.toFloat())
            fileSizeInKB > 0 -> String.format("%d KB", fileSizeInKB)
            else -> String.format("%d bytes", fileSizeInBytes)
        }
    }
    override fun onUserDelete(user: UsersWithAreaNames) {

        try {
            val builder = AlertDialog.Builder(this@SyncUserActivity)
            builder.setTitle(getString(R.string.delete))
                .setIcon(R.drawable.ic_delete)
                .setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_beneficiary_details))
                .setPositiveButton(getString(R.string.yes)) { xx, yy ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val count=userDao.deleteUserById(user.id)
                        if(count>0){
                            val filesList= mutableListOf<Uri>()
                            filesList.add(Uri.parse(user.aadharIdCardPhoto))
                            filesList.add(Uri.parse(user.beneficaryPhoto))
                            filesList.add(Uri.parse(user.gramsevakIdCardPhoto))
                            filesList.add(Uri.parse(user.tabletImeiPhoto))
                            deleteFilesFromFolder(filesList)
                            fetchUserList()
                            withContext(Dispatchers.Main){
                                dialog.dismiss()
                                xx.dismiss()
                            }
                        }

                    }
                }
                .setNegativeButton(getString(R.string.no), null) // If "No" is clicked, do nothing
                .show()
        } catch (e: Exception) {
            Log.d("mytag", "Failed to delete file: ${e.message}",e)
            e.printStackTrace()
        }
    }

}