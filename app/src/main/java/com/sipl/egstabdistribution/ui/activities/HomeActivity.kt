package com.sipl.egstabdistribution.ui.activities

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.permissionx.guolindev.PermissionX
import com.sipl.egstabdistribution.R
import com.sipl.egstabdistribution.database.AppDatabase
import com.sipl.egstabdistribution.database.dao.UserDao
import com.sipl.egstabdistribution.databinding.ActivityCameraBinding
import com.sipl.egstabdistribution.databinding.ActivityHomeBinding
import com.sipl.egstabdistribution.ui.start.LoginActivity
import com.sipl.egstabdistribution.ui.start.SplashActivity
import com.sipl.egstabdistribution.utils.MySharedPref
import com.sipl.egstabdistribution.utils.NoInternetDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    private var isInternetAvailable=false
    private lateinit var noInternetDialog: NoInternetDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mySharedPref=MySharedPref(this)
        if(!mySharedPref.getIsLoggedIn()){
            val intent= Intent(this@HomeActivity, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        database= AppDatabase.getDatabase(this)
        userDao=database.userDao()
        binding.floatingActionButton.setOnClickListener {
            val intent=Intent(this,RegistrationActivity::class.java)
            startActivity(intent)
        }
        binding.layoutLabourRegistrationsOffline.setOnClickListener {
            val intent=Intent(this,SyncUserActivity::class.java)
            startActivity(intent)
        }
        binding.layoutAddedCount.setOnClickListener {
            val intent=Intent(this,BeneficiaryListActivity::class.java)
            startActivity(intent)
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                val builder = AlertDialog.Builder(this@HomeActivity)
                builder.setTitle(getString(R.string.exit))
                    .setMessage(getString(R.string.are_you_sure_you_want_to_exit_app))
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        // If "Yes" is clicked, exit the app
                        finish()
                    }
                    .setNegativeButton(getString(R.string.no), null) // If "No" is clicked, do nothing
                    .show()

            }
        })
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
        requestThePermissions()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId== R.id.action_logout){
            AlertDialog.Builder(this)
                .setMessage(getString(R.string.are_you_sure_you_want_to_logout))
                .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                    // Open the location settings to enable GPS
                    val mySharedPref= MySharedPref(this@HomeActivity)
                    mySharedPref.clearAll()
                    val intent= Intent(this@HomeActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                    dialog.dismiss()
                    // Handle the case when the user chooses not to enable GPS
                }
                .show()

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        try {
            CoroutineScope(Dispatchers.IO).launch{
                val labourCount=userDao.getUsersCount();
                withContext(Dispatchers.Main) {
                    binding.tvRegistrationCount.setText("${labourCount}")

                }
            }
        } catch (e: Exception) {
            Log.d("mytag","Exception",e)
            e.printStackTrace()
        }
    }
    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    private fun requestThePermissions() {
        Log.d("mytag", "request permission called")
        try {
            if (checkPermissions()) {
                // All permissions are granted
               // handlePermissionsGranted()
            } else {
                // Request permissions
                ActivityCompat.requestPermissions(
                    this@HomeActivity,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.CAMERA
                    ),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        } catch (e: Exception) {
            Log.d("mytag", "Exception", e)
            e.printStackTrace()
        }
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this@HomeActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this@HomeActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this@HomeActivity,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // All permissions are granted
                //handlePermissionsGranted()
            } else {
                // Permissions are denied, show permission denied dialog
                showPermissionDeniedDialog()
            }
        }
    }
    private fun showPermissionDeniedDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this@HomeActivity)
        alertDialogBuilder.apply {
            setTitle("Permission Denied")
            setMessage("You have denied the permission. Please enable it in the app settings.")
            setPositiveButton("Settings") { dialogInterface: DialogInterface, i: Int ->
                navigateToAppSettings()
                dialogInterface.dismiss()
            }
            setNegativeButton("Cancel") { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss()
            }
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun navigateToAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }


}