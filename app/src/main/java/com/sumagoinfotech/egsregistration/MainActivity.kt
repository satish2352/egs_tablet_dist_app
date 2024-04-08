package com.sumagoinfotech.egsregistration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.sumagoinfotech.egsregistration.adapter.MyPageNumberAdapter
import com.sumagoinfotech.egsregistration.adapter.TabDistributionListAdapter
import com.sumagoinfotech.egsregistration.databinding.ActivityMainBinding
import com.sumagoinfotech.egsregistration.model.TabDistList.TabUser
import com.sumagoinfotech.egsregistration.ui.LoginActivity
import com.sumagoinfotech.egsregistration.ui.RegistrationActivity
import com.sumagoinfotech.egsregistration.utils.CustomProgressDialog
import com.sumagoinfotech.egsregistration.utils.MySharedPref
import com.sumagoinfotech.egsregistration.webservice.ApiClient
import com.sumagoinfotech.egsregistration.webservice.ApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity(),
    MyPageNumberAdapter.OnPageNumberClickListener {
    lateinit var binding:ActivityMainBinding
    private lateinit var dialog: CustomProgressDialog
    private  var isInternetAvailable=false
    private lateinit var apiService: ApiService
    private lateinit var adapter:TabDistributionListAdapter
    private var tabUserList= mutableListOf<TabUser>()
    private var pageSize="25"
    private lateinit var paginationAdapter: MyPageNumberAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title=resources.getString(R.string.egs_tab_distribution)
        dialog= CustomProgressDialog(this)
        apiService= ApiClient.create(this)
        adapter= TabDistributionListAdapter(tabUserList)
        binding.recyclerView.layoutManager= LinearLayoutManager(this, RecyclerView.VERTICAL,false)
        binding.recyclerViewPageNumbers.layoutManager= LinearLayoutManager(this, RecyclerView.HORIZONTAL,false)
        binding.recyclerView.adapter=adapter
        binding.recyclerView.setScrollbarFadingEnabled(false)
        binding.recyclerViewPageNumbers.setScrollbarFadingEnabled(false)
        adapter.notifyDataSetChanged()
        paginationAdapter= MyPageNumberAdapter(0,this)
        ReactiveNetwork
            .observeNetworkConnectivity(applicationContext)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ connectivity: Connectivity ->
                Log.d("##", "=>" + connectivity.state())
                if (connectivity.state().toString() == "CONNECTED") {
                    isInternetAvailable = true
                } else {
                    isInternetAvailable = false

                }
            }) { throwable: Throwable? -> }

        binding.tvNoRecords.visibility=View.GONE

       /* val pageNumberRecyclerView = findViewById<PageNumberRecyclerView>(R.id.recyclerViewPageNumbers);
        pageNumberRecyclerView.setTotalPages(20, this);*/

        binding.floatingActionButton.setOnClickListener {

            val intent=Intent(this,RegistrationActivity::class.java)
            startActivity(intent)
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



    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch {
            getDataFromServer("1",pageSize)
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
                                adapter= TabDistributionListAdapter(tabUserList)
                                Log.d("mytag",""+tabUserList.size)
                                Log.d("mytag",""+response.body()?.status)
                                binding.recyclerView.adapter=adapter
                                adapter.notifyDataSetChanged()
                                val pageAdapter=MyPageNumberAdapter(response.body()?.totalPages!!,this@MainActivity)
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
        CoroutineScope(Dispatchers.IO).launch {
            getDataFromServer("$pageNumber",pageSize)
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
}