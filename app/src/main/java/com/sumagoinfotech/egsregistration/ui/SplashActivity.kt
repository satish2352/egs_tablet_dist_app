package com.sumagoinfotech.egsregistration.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.google.gson.Gson
import com.sumagoinfotech.egsregistration.MainActivity
import com.sumagoinfotech.egsregistration.R
import com.sumagoinfotech.egsregistration.database.AppDatabase
import com.sumagoinfotech.egsregistration.database.AreaDao
import com.sumagoinfotech.egsregistration.database.AreaItem
import com.sumagoinfotech.egsregistration.databinding.ActivitySplashBinding
import com.sumagoinfotech.egsregistration.utils.MySharedPref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                if(areaDao.getAllArea().isEmpty())
                {
                    val items = readJsonFromAssets(this@SplashActivity, "address.json")
                    areaDao.insertInitialRecords(items)
                    val size=areaDao.getAllArea().size;
                    Log.d("mytag","Area Entries $size")
                    if(size==44342){
                        mySharedPref.setAllAreaEntries(true)
                    }else{
                        mySharedPref.setAllAreaEntries(false)
                    }
                }else{
                    Log.d("mytag","Not empty")
                }
            //fetchMastersFromServer()
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

    /*private  fun fetchMastersFromServer(){
        try {
            val apiService= ApiClient.create(this@SplashActivity)
            apiService.getAllMasters().enqueue(object :
                Callback<MastersModel> {
                override fun onResponse(
                    call: Call<MastersModel>,
                    response: Response<MastersModel>
                ) {
                    if(response.isSuccessful){
                        if(response.body()?.status.equals("success")) {
                            val skillsConverted=mapToSkills(response?.body()?.data?.skills!!)
                            val maritalStatusConverted=mapToMaritalStatus(response?.body()?.data?.maritalstatus!!)
                            val genderConverted=mapToMaritalGender(response?.body()?.data?.gender!!)
                            val relationConverted=mapToRelation(response?.body()?.data?.relation!!)
                            val documentTypeConverted=mapToDocumentType(response?.body()?.data?.documenttype!!)
                            val registrationStatusConverted=mapToRegistrationStatus(response?.body()?.data?.registrationstatus!!)
                            val reasonsConverted=mapToReasons(response?.body()?.data?.reasons!!)
                            val documentReasonsConverted=mapToDocumentReasons(response?.body()?.data?.documentreasons!!)
                            CoroutineScope(Dispatchers.IO).launch {
                                skillsDao.insertInitialRecords(skillsConverted)
                                maritalStatusDao.insertInitialRecords(maritalStatusConverted)
                                genderDao.insertInitialRecords(genderConverted)
                                relationDao.insertInitialRecords(relationConverted)
                                documentTypeDropDownDao.insertInitialRecords(documentTypeConverted)
                                registrationStatusDao.insertInitialRecords(registrationStatusConverted)
                                registrationStatusDao.insertInitialRecords(registrationStatusConverted)
                                reasonsDao.insertInitialRecords(reasonsConverted)
                                documentReasonsDao.insertInitialRecords(documentReasonsConverted)
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
                override fun onFailure(call: Call<MastersModel>, t: Throwable) {
                    Log.d("mytag","fetchMastersFromServer:onFailure ${t.message}")
                    Toast.makeText(this@SplashActivity, "Error Ocuured during api call", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Log.d("mytag","Exception: "+e.message)
            e.printStackTrace()
        }
    }*/



    /*fun mapToMaritalGender(apiResponseList: List<Gender>): List<com.sumagoinfotech.digicopy.database.entity.Gender> {
        return apiResponseList.map { apiResponse ->
            com.sumagoinfotech.digicopy.database.entity.Gender(
                id = apiResponse.id,
                gender_name = apiResponse.gender_name,
                is_active = apiResponse.is_active,
                created_at = apiResponse.created_at,
                updated_at = apiResponse.updated_at
            )
        }
    }*/



}