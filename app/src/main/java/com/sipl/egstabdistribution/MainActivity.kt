package com.sipl.egstabdistribution

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.sipl.egstabdistribution.adapter.MyPageNumberAdapter
import com.sipl.egstabdistribution.adapter.TabDistributionListAdapter
import com.sipl.egstabdistribution.databinding.ActivityMainBinding
import com.sipl.egstabdistribution.model.TabDistList.TabUser
import com.sipl.egstabdistribution.ui.BeneficiaryDetailsActivity
import com.sipl.egstabdistribution.ui.LoginActivity
import com.sipl.egstabdistribution.ui.RegistrationActivity
import com.sipl.egstabdistribution.utils.CustomProgressDialog
import com.sipl.egstabdistribution.utils.MySharedPref
import com.sipl.egstabdistribution.webservice.ApiClient
import com.sipl.egstabdistribution.webservice.ApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity(),
    MyPageNumberAdapter.OnPageNumberClickListener,TabDistributionListAdapter.OnBeneficiaryClickListener {
    lateinit var binding: ActivityMainBinding
    private lateinit var dialog: CustomProgressDialog
    private  var isInternetAvailable=false
    private lateinit var apiService: ApiService
    private lateinit var adapter:TabDistributionListAdapter
    private var tabUserList= mutableListOf<TabUser>()
    private var pageSize=50
    private lateinit var paginationAdapter: MyPageNumberAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title=resources.getString(R.string.beneficiary_list)
        dialog= CustomProgressDialog(this)
        apiService= ApiClient.create(this)
        adapter= TabDistributionListAdapter(tabUserList,0,pageSize,this)
        binding.recyclerView.layoutManager= LinearLayoutManager(this, RecyclerView.VERTICAL,false)
        binding.recyclerViewPageNumbers.layoutManager= LinearLayoutManager(this, RecyclerView.HORIZONTAL,false)
        binding.recyclerView.adapter=adapter
        binding.recyclerView.setScrollbarFadingEnabled(false)
        binding.recyclerViewPageNumbers.setScrollbarFadingEnabled(false)
        adapter.notifyDataSetChanged()
        paginationAdapter= MyPageNumberAdapter(0,"0",this)
        ReactiveNetwork
            .observeNetworkConnectivity(applicationContext)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ connectivity: Connectivity ->
                Log.d("##", "=>" + connectivity.state())
                if (connectivity.state().toString() == "CONNECTED") {
                    isInternetAvailable = true
                    CoroutineScope(Dispatchers.IO).launch {
                        getDataFromServer("1",pageSize.toString())
                    }
                } else {
                    isInternetAvailable = false

                }
            }) { throwable: Throwable? -> }

        binding.tvNoRecords.visibility=View.GONE

       /* val pageNumberRecyclerView = findViewById<PageNumberRecyclerView>(R.id.recyclerViewPageNumbers);
        pageNumberRecyclerView.setTotalPages(20, this);*/

        binding.floatingActionButton.setOnClickListener {

            if(isInternetAvailable){
                val intent=Intent(this,RegistrationActivity::class.java)
                startActivity(intent)
            }else{
                Toast.makeText(this@MainActivity,
                    getString(R.string.please_check_internet_connection),Toast.LENGTH_LONG).show()
            }


        }
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dx > 0) {
                    // Scrolling towards the right
                    binding.layoutIconScroll.iVScrollableIcon.visibility = View.VISIBLE
                } else if (dx < 0) {
                    // Scrolling towards the left
                    binding.layoutIconScroll.iVScrollableIcon.visibility = View.VISIBLE
                } else {
                    // No horizontal scrolling (stopped)
                    binding.layoutIconScroll.iVScrollableIcon.visibility = View.GONE
                }
            }
        })

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                val builder = AlertDialog.Builder(this@MainActivity)
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

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch {
            getDataFromServer("1",pageSize.toString())
        }
    }

    private suspend fun getDataFromServer(startPageNumber: String,pageLength:String){
        runOnUiThread {
            dialog.show()
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response=apiService.getTabDistributionList(startPageNumber = startPageNumber, pageLength = pageLength)
                if(response.isSuccessful){
                    if(response.body()?.status.equals("true"))
                    {
                        if(!response.body()?.data.isNullOrEmpty()) {
                            withContext(Dispatchers.Main){
                                binding.tvNoRecords.visibility=View.GONE
                                tabUserList= response.body()?.data?.toMutableList()!!
                                adapter= TabDistributionListAdapter(tabUserList,Integer.parseInt(response.body()?.page_no_to_hilight),pageSize,this@MainActivity)
                                Log.d("mytag",""+tabUserList.size)
                                Log.d("mytag",""+response.body()?.status)
                                binding.recyclerView.adapter=adapter
                                adapter.notifyDataSetChanged()
                                val pageAdapter=MyPageNumberAdapter(response.body()?.totalPages!!,response.body()?.page_no_to_hilight.toString(),this@MainActivity)
                                if(binding.recyclerView.computeHorizontalScrollRange()>binding.recyclerView.width)
                                {
                                    binding.layoutIconScroll.iVScrollableIcon.visibility = View.VISIBLE

                                }else{
                                    binding.layoutIconScroll.iVScrollableIcon.visibility = View.GONE
                                }
                                binding.recyclerViewPageNumbers.adapter=pageAdapter
                                pageAdapter.notifyDataSetChanged()
                                Toast.makeText(this@MainActivity,response.body()?.message,
                                    Toast.LENGTH_SHORT).show()

                            }
                        }else{

                           runOnUiThread {  binding.tvNoRecords.visibility=View.VISIBLE }
                        }

                    }else{
                        withContext(Dispatchers.Main){
                            Toast.makeText(this@MainActivity,resources.getString(R.string.response_unsuccessfull),
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    Log.d("mytag",""+response.body()?.message)
                    Log.d("mytag",""+response.body()?.status)
                }else{

                    withContext(Dispatchers.Main){
                        Toast.makeText(this@MainActivity,resources.getString(R.string.response_unsuccessfull),
                            Toast.LENGTH_SHORT).show()

                    }
                }
                runOnUiThread {dialog.dismiss()  }
            } catch (e: Exception) {
                runOnUiThread { dialog.dismiss() }
                withContext(Dispatchers.Main){
                    Toast.makeText(this@MainActivity,resources.getString(R.string.response_failed),
                        Toast.LENGTH_SHORT).show()
                    runOnUiThread {  binding.tvNoRecords.visibility=View.VISIBLE }
                }
                Log.d("mytag","ListActivity :: getDataFromServer "+e.message)
            }
        }
    }

    override fun onPageNumberClicked(pageNumber: Int) {
        Log.d("mytag","ListActivity :: getDataFromServer "+pageNumber)
        CoroutineScope(Dispatchers.IO).launch {
            getDataFromServer("$pageNumber",pageSize.toString())
            paginationAdapter.setSelectedPage(pageNumber)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.action_logout){
            AlertDialog.Builder(this)
                .setMessage("Are you sure you want to logout ?")
                .setPositiveButton("Yes") { dialog, _ ->
                    // Open the location settings to enable GPS
                    val mySharedPref= MySharedPref(this@MainActivity)
                    mySharedPref.clearAll()
                    val intent= Intent(this@MainActivity, LoginActivity::class.java)
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

    override fun onClick(user: TabUser) {

        if(isInternetAvailable){
            val intent= Intent(this@MainActivity, BeneficiaryDetailsActivity::class.java)
            intent.putExtra("id",user.id.toString())
            Log.d("mytag",user.id.toString())
            startActivity(intent)
        }else{

            Toast.makeText(this@MainActivity,
                getString(R.string.please_check_internet_connection),Toast.LENGTH_LONG).show()
        }

    }
}