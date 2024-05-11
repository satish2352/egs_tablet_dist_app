package com.sipl.egstabdistribution.ui.activities

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
import com.sipl.egstabdistribution.R
import com.sipl.egstabdistribution.adapter.MyPageNumberAdapter
import com.sipl.egstabdistribution.adapter.TabDistributionListAdapter
import com.sipl.egstabdistribution.databinding.ActivityMainBinding
import com.sipl.egstabdistribution.model.TabDistList.TabUser
import com.sipl.egstabdistribution.ui.start.LoginActivity
import com.sipl.egstabdistribution.utils.CustomProgressDialog
import com.sipl.egstabdistribution.utils.MySharedPref
import com.sipl.egstabdistribution.utils.NoInternetDialog
import com.sipl.egstabdistribution.webservice.ApiClient
import com.sipl.egstabdistribution.webservice.ApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class BeneficiaryListActivity : AppCompatActivity(),
    MyPageNumberAdapter.OnPageNumberClickListener,TabDistributionListAdapter.OnBeneficiaryClickListener {
    lateinit var binding: ActivityMainBinding
    private lateinit var dialog: CustomProgressDialog
    private  var isInternetAvailable=false
    private lateinit var noInternetDialog: NoInternetDialog
    private lateinit var apiService: ApiService
    private lateinit var adapter:TabDistributionListAdapter
    private var tabUserList= mutableListOf<TabUser>()
    private var pageSize=50
    private lateinit var paginationAdapter: MyPageNumberAdapter
    private var currentPage=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
            currentPage="1"
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
                        CoroutineScope(Dispatchers.IO).launch {
                            getDataFromServer("1",pageSize.toString())
                        }
                    } else {
                        isInternetAvailable = false

                        noInternetDialog.showDialog()
                    }
                }) { throwable: Throwable? -> }

            binding.tvNoRecords.visibility=View.GONE

            /* val pageNumberRecyclerView = findViewById<PageNumberRecyclerView>(R.id.recyclerViewPageNumbers);
                  pageNumberRecyclerView.setTotalPages(20, this);*/

            binding.floatingActionButton.setOnClickListener {
                if(isInternetAvailable){
                    val intent=Intent(this, RegistrationActivity::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(this@BeneficiaryListActivity,
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
        } catch (e: Exception) {
            Log.d("mytag","Exception",e)
            e.printStackTrace()
        }


    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch {

                getDataFromServer(currentPage,pageSize.toString())

        }
    }


    private suspend fun getDataFromServer(startPageNumber: String,pageLength:String){
        try {
            runOnUiThread {
                dialog.show()
            }
        } catch (e: Exception) {
            Log.d("mytag","Exception",e)
            e.printStackTrace()
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
                                adapter= TabDistributionListAdapter(tabUserList,Integer.parseInt(response.body()?.page_no_to_hilight),pageSize,this@BeneficiaryListActivity)
                                binding.recyclerView.adapter=adapter
                                adapter.notifyDataSetChanged()
                                val pageAdapter=MyPageNumberAdapter(response.body()?.totalPages!!,response.body()?.page_no_to_hilight.toString(),this@BeneficiaryListActivity)
                                if(binding.recyclerView.computeHorizontalScrollRange()>binding.recyclerView.width)
                                {
                                    binding.layoutIconScroll.iVScrollableIcon.visibility = View.VISIBLE

                                }else{
                                    binding.layoutIconScroll.iVScrollableIcon.visibility = View.GONE
                                }
                                binding.recyclerViewPageNumbers.adapter=pageAdapter
                                pageAdapter.notifyDataSetChanged()

                            }
                        }else{
                           runOnUiThread {
                               tabUserList.clear()
                               adapter.notifyDataSetChanged()
                               binding.tvNoRecords.visibility=View.VISIBLE
                               paginationAdapter= MyPageNumberAdapter(0,"0",this@BeneficiaryListActivity)
                               binding.recyclerViewPageNumbers.adapter=paginationAdapter
                               paginationAdapter.notifyDataSetChanged()

                           }
                        }

                    }else{
                        withContext(Dispatchers.Main){
                            Toast.makeText(this@BeneficiaryListActivity,resources.getString(R.string.response_unsuccessfull),
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@BeneficiaryListActivity,resources.getString(R.string.response_unsuccessfull),
                            Toast.LENGTH_SHORT).show()

                    }
                }
                runOnUiThread {dialog.dismiss()  }
            } catch (e: Exception) {
                runOnUiThread { dialog.dismiss() }
                withContext(Dispatchers.Main){
                    Toast.makeText(this@BeneficiaryListActivity,resources.getString(R.string.response_failed),
                        Toast.LENGTH_SHORT).show()
                    runOnUiThread {  binding.tvNoRecords.visibility=View.VISIBLE }
                }
                Log.d("mytag","ListActivity :: getDataFromServer "+e.message)
            }
        }
    }

    override fun onPageNumberClicked(pageNumber: Int) {
        try {
            if (isInternetAvailable) {
                currentPage="$pageNumber"
                Log.d("mytag","ListActivity :: getDataFromServer "+pageNumber)
                CoroutineScope(Dispatchers.IO).launch {
                    getDataFromServer("$pageNumber",pageSize.toString())
                    paginationAdapter.setSelectedPage(pageNumber)
                }
            }else{
                Toast.makeText(this@BeneficiaryListActivity,resources.getString(R.string.check_internet_connection),Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.d("mytag","Exception",e)
            e.printStackTrace()
        }
    }

    override fun onClick(user: TabUser) {

        if(isInternetAvailable){
            val intent= Intent(this@BeneficiaryListActivity, BeneficiaryDetailsActivity::class.java)
            intent.putExtra("id",user.id.toString())
            Log.d("mytag",user.id.toString())
            startActivity(intent)
        }else{

            Toast.makeText(this@BeneficiaryListActivity,
                getString(R.string.please_check_internet_connection),Toast.LENGTH_LONG).show()
        }

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}