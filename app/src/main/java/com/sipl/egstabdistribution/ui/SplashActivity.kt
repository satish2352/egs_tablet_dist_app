package com.sipl.egstabdistribution.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.google.gson.Gson
import com.sipl.egstabdistribution.MainActivity
import com.sipl.egstabdistribution.database.AppDatabase
import com.sipl.egstabdistribution.database.AreaDao
import com.sipl.egstabdistribution.database.AreaItem
import com.sipl.egstabdistribution.databinding.ActivitySplashBinding
import com.sipl.egstabdistribution.model.areamaster.AreaUpdateData
import com.sipl.egstabdistribution.model.areamaster.MasterUpdateModel
import com.sipl.egstabdistribution.utils.MySharedPref
import com.sipl.egstabdistribution.webservice.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var appDatabase: AppDatabase;
    private lateinit var areaDao: AreaDao
    private lateinit var mySharedPref: MySharedPref
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySplashBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        mySharedPref = MySharedPref(this)
        appDatabase=AppDatabase.getDatabase(this)
        areaDao=appDatabase.areaDao()
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        mySharedPref.setDeviceId(deviceId)
        binding.progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            mySharedPref.setDeviceId(deviceId)
            if(!mySharedPref.getAllAreaEntries())
                if(areaDao.getAllArea().size<44342)
                {
                    val waitingJob=async {
                        val items = readJsonFromAssets(this@SplashActivity, "address.json")
                        areaDao.insertInitialRecords(items)
                        val size=areaDao.getAllArea().size;
                        Log.d("mytag","Area Entries $size")
                        if(size==44342){
                            mySharedPref.setAllAreaEntries(true)
                        }else{
                            mySharedPref.setAllAreaEntries(false)
                        }
                    }
                    waitingJob.await()
                }else{
                    Log.d("mytag","Not empty")
                }

            val masterWaitingJob=async {   fetchMastersFromServer() }
            masterWaitingJob.await()
            withContext(Dispatchers.Main) {
                val mySharedPref=MySharedPref(this@SplashActivity)
                if(mySharedPref.getIsLoggedIn()){
                    binding.progressBar.visibility = View.GONE
                    val intent= Intent(this@SplashActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }else{
                    binding.progressBar.visibility = View.GONE
                    val intent= Intent(this@SplashActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
    private  fun readJsonFromAssets(context: Context, fileName: String): List<AreaItem> {
        val items: MutableList<AreaItem> = mutableListOf()
        try {
            // Step 3: Open and read the JSON file using the AssetManager
            val inputStream = context.assets.open(fileName)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))

            // Step 4: Parse JSON data using Gson
            val gson = Gson()
            val jsonContent = bufferedReader.use { it.readText() }
            val itemList = gson.fromJson(jsonContent, Array<AreaItem>::class.java)

            // Step 5: Convert JSON data into a list of objects
            items.addAll(itemList)

            // Close the input stream
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("mytag","readJsonFromAssets "+e.message)
        }
        return items
    }

    private  fun fetchMastersFromServer(){
        try {
            val apiService= ApiClient.create(this@SplashActivity)
            apiService.fetchMastersDataTobeUpdated().enqueue(object :
                Callback<MasterUpdateModel> {
                override fun onResponse(
                    call: Call<MasterUpdateModel>,
                    response: Response<MasterUpdateModel>
                ) {
                    if(response.isSuccessful){
                        if(response.body()?.status.equals("success"))
                        {
                            if(response.body()?.data?.size!!>0){
                                CoroutineScope(Dispatchers.IO).launch {
                                    val documentReasonsConverted=mapDataToArea(response?.body()?.data!!)
                                    documentReasonsConverted.forEach { entity ->
                                        val existingEntity = areaDao.getAreaByLocationId(entity.location_id)
                                        if (existingEntity != null) {
                                            // Update existing entity
                                            entity.id = existingEntity.id
                                            areaDao.update(entity)
                                        } else {
                                            // Insert new entity
                                            areaDao.insert(entity)
                                        }
                                    }
                                }
                            }

                        }else {
                            Log.d("mytag","fetchMastersFromServer:Response Not success")
                            Toast.makeText(this@SplashActivity, "No records found", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else{
                        Log.d("mytag","fetchMastersFromServer:Response unsuccessful")
                        Toast.makeText(this@SplashActivity, "Response unsuccessful", Toast.LENGTH_SHORT).show()
                    }

                }
                override fun onFailure(call: Call<MasterUpdateModel>, t: Throwable) {
                    Log.d("mytag","fetchMastersFromServer:onFailure ${t.message}")
                    Toast.makeText(this@SplashActivity, "Error Ocuured during api call", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Log.d("mytag","Exception: "+e.message)
            e.printStackTrace()
        }
    }
    fun mapDataToArea(apiResponseList: List<AreaUpdateData>): List<AreaItem> {
        return apiResponseList.map { apiResponse ->
            AreaItem(
                parent_id = apiResponse.parent_id.toString(),
                is_active = apiResponse.is_active,
                is_visible = apiResponse.is_visible,
                location_id = apiResponse.location_id.toString(),
                location_type = apiResponse.location_type,
                name = apiResponse.name
            )
        }
    }



}