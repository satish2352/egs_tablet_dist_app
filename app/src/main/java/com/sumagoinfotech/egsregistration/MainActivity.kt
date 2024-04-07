package com.sumagoinfotech.egsregistration

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import com.sumagoinfotech.egsregistration.databinding.ActivityMainBinding
import com.sumagoinfotech.egsregistration.ui.LoginActivity
import com.sumagoinfotech.egsregistration.ui.RegistrationActivity
import com.sumagoinfotech.egsregistration.utils.MySharedPref

class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title=resources.getString(R.string.egs)
        binding.floatingActionButton.setOnClickListener {
            val intent=Intent(this,RegistrationActivity::class.java)
            startActivity(intent)
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle(getString(R.string.exit))
                        .setMessage(getString(R.string.are_you_sure_you_want_to_exit_app))
                        .setPositiveButton(getString(R.string.yes)) { _, _ ->
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

        if(item.itemId==R.id.action_logout)
        {
            AlertDialog.Builder(this)
                .setMessage("Are you sure you want to logout ?")
                .setPositiveButton("Yes") { dialog, _ ->
                    // Open the location settings to enable GPS
                    val mySharedPref= MySharedPref(this@MainActivity)
                    mySharedPref.clearAll()
                    val intent=Intent(this@MainActivity, LoginActivity::class.java)
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
}