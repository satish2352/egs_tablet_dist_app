package com.sipl.egstabdistribution.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import com.sipl.egstabdistribution.R
import com.sipl.egstabdistribution.database.AppDatabase
import com.sipl.egstabdistribution.database.dao.UserDao
import com.sipl.egstabdistribution.databinding.ActivityCameraBinding
import com.sipl.egstabdistribution.databinding.ActivityHomeBinding
import com.sipl.egstabdistribution.ui.start.LoginActivity
import com.sipl.egstabdistribution.utils.MySharedPref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId== R.id.action_logout){
            AlertDialog.Builder(this)
                .setMessage("Are you sure you want to logout ?")
                .setPositiveButton("Yes") { dialog, _ ->
                    // Open the location settings to enable GPS
                    val mySharedPref= MySharedPref(this@HomeActivity)
                    mySharedPref.clearAll()
                    val intent= Intent(this@HomeActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
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

    override fun onResume() {
        super.onResume()

        CoroutineScope(Dispatchers.IO).launch{
            val labourCount=userDao.getUsersCount();
            withContext(Dispatchers.Main) {
                binding.tvRegistrationCount.setText("${labourCount}")
            }
        }
    }


}