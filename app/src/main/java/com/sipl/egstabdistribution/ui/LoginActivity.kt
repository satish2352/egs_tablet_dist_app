package com.sipl.egstabdistribution.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.sipl.egstabdistribution.MainActivity
import com.sipl.egstabdistribution.R
import com.sipl.egstabdistribution.database.AppDatabase
import com.sipl.egstabdistribution.databinding.ActivityLoginBinding
import com.sipl.egstabdistribution.model.LoginModel
import com.sipl.egstabdistribution.utils.CustomProgressDialog
import com.sipl.egstabdistribution.utils.MySharedPref
import com.sipl.egstabdistribution.utils.MyValidator
import com.sipl.egstabdistribution.webservice.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var database: AppDatabase
    private lateinit var customProgressDialog: CustomProgressDialog
    private lateinit var mySharedPref: MySharedPref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        database= AppDatabase.getDatabase(this)
        mySharedPref=MySharedPref(this)
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        mySharedPref.setDeviceId(deviceId)
        customProgressDialog= CustomProgressDialog(this)
        binding.btnLogin.setOnClickListener {
            if(validateFields()) {
                customProgressDialog.show()
                val mySharedPref=MySharedPref(this@LoginActivity)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val apiService= ApiClient.create(this@LoginActivity)
                        val call=apiService.loginUser(binding.etEmail.text.toString(),binding.etPassword.text.toString())
                        call.enqueue(object : Callback<LoginModel> {
                            override fun onResponse(
                                call: Call<LoginModel>,
                                response: Response<LoginModel>
                            ) {
                                if(response.isSuccessful)
                                {
                                    val message=response.body()?.message
                                    if(response.body()?.status.equals("True")){
                                        val loginModel=response.body()
                                        mySharedPref.setIsLoggedIn(true)
                                        mySharedPref.setId(loginModel?.data?.id!!)
                                        mySharedPref.setEmail(loginModel?.data?.email!!)
                                        mySharedPref.setRememberToken(loginModel?.data?.remember_token!!)
                                        mySharedPref.setRoleId(loginModel?.data?.role_id!!)
                                        mySharedPref.setFName(loginModel?.data?.f_name!!)
                                        mySharedPref.setMName(loginModel?.data?.m_name!!)
                                        mySharedPref.setLName(loginModel?.data?.l_name!!)
                                        mySharedPref.setUserDistrictId(loginModel?.data?.user_district.toString())
                                        mySharedPref.setUserTalukaId(loginModel?.data?.user_taluka.toString())
                                        mySharedPref.setUserVillageId(loginModel?.data?.user_village.toString())
                                        if(loginModel?.data?.role_id==2)
                                        {
                                            mySharedPref.setOfficerDistrictID(loginModel?.data?.user_district.toString())
                                            mySharedPref.setUserTalukaId(loginModel?.data?.user_taluka.toString())
                                            mySharedPref.setUserVillageId(loginModel?.data?.user_village.toString())
                                        }
                                        runOnUiThread {
                                            customProgressDialog.dismiss()
                                                val toast= Toast.makeText(this@LoginActivity,
                                                    getString(R.string.login_successful),
                                                    Toast.LENGTH_SHORT)
                                                toast.show()
                                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                                startActivity(intent)
                                                finish()
                                        }
                                    }else{
                                        runOnUiThread {
                                            customProgressDialog.dismiss()
                                            val toast= Toast.makeText(this@LoginActivity,
                                                message,
                                                Toast.LENGTH_SHORT)
                                            toast.show()
                                        }
                                    }

                                }else{
                                    runOnUiThread {
                                        customProgressDialog.dismiss()
                                        val toast= Toast.makeText(this@LoginActivity,
                                            getString(R.string.error_while_login),
                                            Toast.LENGTH_SHORT)
                                        toast.show()
                                    }
                                }
                            }
                            override fun onFailure(call: Call<LoginModel>, t: Throwable) {
                                runOnUiThread {
                                    customProgressDialog.dismiss()
                                    val toast= Toast.makeText(this@LoginActivity,
                                        getString(R.string.error_while_login),
                                        Toast.LENGTH_SHORT)
                                    toast.show()
                                }
                            }
                        })
                        /*if(user!==null){
                            Log.d("mytag","found "+user.email)
                            runOnUiThread {
                                val toast= Toast.makeText(this@LoginActivity,"Login successful",
                                    Toast.LENGTH_SHORT)
                                toast.show()
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()
                            }
                        }else{
                            runOnUiThread {
                                val toast= Toast.makeText(this@LoginActivity,"Please enter correct details",
                                    Toast.LENGTH_SHORT)
                                toast.show()
                            }
                        }*/
                    } catch (e: Exception) {
                        Log.d("mytag","Exception Inserted : ${e.message}")
                        e.printStackTrace()
                    }
                }
            }else{

            }
        }
    }

    private fun validateFields(): Boolean {
        var list=ArrayList<Boolean>()
        if(MyValidator.isValidEmail(binding.etEmail.text.toString())){

            binding.txLayoutEmail.error=null
            list.add(true)
        }else{
            list.add(false)
            binding.txLayoutEmail.error="Please Enter Valid Email"
        }
        if(MyValidator.isValidPassword(binding.etPassword.text.toString())){
            binding.txLayoutPassword.error=null
            list.add(true)
        }else{
            list.add(false)
            binding.txLayoutPassword.error="Please enter at least 8 digit password "
        }

        return !list.contains(false)
    }
}