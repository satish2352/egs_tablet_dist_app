package com.sipl.egstabdistribution.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.permissionx.guolindev.PermissionX
import com.sipl.egstabdistribution.R
import com.sipl.egstabdistribution.camera.CameraActivity
import com.sipl.egstabdistribution.database.AppDatabase
import com.sipl.egstabdistribution.database.dao.AreaDao
import com.sipl.egstabdistribution.database.dao.UserDao
import com.sipl.egstabdistribution.database.entity.AreaItem
import com.sipl.egstabdistribution.database.entity.User
import com.sipl.egstabdistribution.databinding.ActivityRegistrationBinding
import com.sipl.egstabdistribution.utils.CustomProgressDialog
import com.sipl.egstabdistribution.utils.MyValidator
import com.sipl.egstabdistribution.webservice.ApiClient
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import id.zelory.compressor.constraint.size
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

class RegistrationActivity : AppCompatActivity() {
    private  var isInternetAvailable=false
    private  var latitude:Double=0.0
    private  var longitude:Double=0.0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var appDatabase: AppDatabase
    private lateinit var areaDao: AreaDao
    private lateinit var userDao: UserDao
    private lateinit var districtList:List<AreaItem>
    private lateinit var villageList:List<AreaItem>
    private lateinit var talukaList:List<AreaItem>
    private var districtNames= mutableListOf<String>()
    private var villageNames= mutableListOf<String>()
    private var talukaNames= mutableListOf<String>()
    private var districtId=""
    private var villageId=""
    private var talukaId=""
    private  var addressFromLatLong:String=""
    private lateinit var binding:ActivityRegistrationBinding
    private val REQUEST_CODE_AADHAR_CARD = 100
    private  val REQUEST_CODE_PHOTO = 200
    private  val REQUEST_CODE_GRAMSEVAK = 300
    private  val REQUEST_CODE_TABLET_IMEI = 400
    private  var aadharIdImagePath:String=""
    private  var photoImagePath:String=""
    private  var gramsevakIdImagePath:String=""
    private  var tabletImeiPhotoPath:String=""
    private lateinit var dialog: CustomProgressDialog
    private var isAadharVerified=false
    private lateinit var aadharCardFile:File
    private lateinit var beneficiaryPhotoFile:File
    private lateinit var imeiPhotoFile:File
    private lateinit var gramsevakIdPhotoFile:File

    private lateinit var locationManager: LocationManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog= CustomProgressDialog(this)
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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.beneficiary_registration)
        try {
            appDatabase=AppDatabase.getDatabase(this)
            areaDao=appDatabase.areaDao()
            userDao=appDatabase.userDao()
            binding.etGramPanchayatName.visibility=View.GONE
            val emptyByteArray = ByteArray(0)
            CoroutineScope(Dispatchers.IO).launch {

                aadharCardFile= createTempJpgFile(this@RegistrationActivity,emptyByteArray,"aadharCardFile")!!
                beneficiaryPhotoFile= createTempJpgFile(this@RegistrationActivity,emptyByteArray,"beneficiaryPhotoFile")!!
                imeiPhotoFile= createTempJpgFile(this@RegistrationActivity,emptyByteArray,"imeiPhotoFile")!!
                gramsevakIdPhotoFile= createTempJpgFile(this@RegistrationActivity,emptyByteArray,"gramsevakIdPhoto")!!
            }
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            binding.btnRegister.setOnClickListener {
              if(validateFieldsX()){
                  if(isInternetAvailable)
                  {
                      CoroutineScope(Dispatchers.Default).launch {
                          val aadharCheckJob=async { checkIfAadharCardExists(binding.etAadharCard.text.toString())}
                          isAadharVerified=aadharCheckJob.await()
                          if(isAadharVerified){
                              saveUserDetails()
                          }
                      }
                  }else{
                      saveUserDetails();
                     // Toast.makeText(this@RegistrationActivity,resources.getString(R.string.check_internet_connection),Toast.LENGTH_LONG).show()
                  }
              }else{
                  Toast.makeText(this@RegistrationActivity,resources.getString(R.string.enter_all_details),Toast.LENGTH_LONG).show()
              }
            }
            binding.ivChangeAadhar.setOnClickListener {

                if (!isLocationEnabled()) {
                    showEnableLocationDialog()
                } else {
                    startCameraActivity(REQUEST_CODE_AADHAR_CARD)
                }

            }
            binding.ivChangePhoto.setOnClickListener {
                if (!isLocationEnabled()) {
                    showEnableLocationDialog()
                } else {
                    startCameraActivity(REQUEST_CODE_PHOTO)
                }
            }
            binding.ivChangeGramsevakId.setOnClickListener {
                if (!isLocationEnabled()) {
                    showEnableLocationDialog()
                } else {
                    startCameraActivity(REQUEST_CODE_GRAMSEVAK)
                }
            }
            binding.ivChangeTabletImei.setOnClickListener {

                if (!isLocationEnabled()) {
                    showEnableLocationDialog()
                } else {
                    startCameraActivity(REQUEST_CODE_TABLET_IMEI)
                }
            }
            CoroutineScope(Dispatchers.IO).launch {
                val waitingJob=async { districtList=areaDao.getAllDistrict() }
                waitingJob.await()
                Log.d("mytag",districtList.size.toString())
                withContext(Dispatchers.Main){
                    for (district in districtList){
                        districtNames.add(district.name)
                    }
                    initializeFields()
                }

            }
        } catch (e: Exception) {
            Log.d("mytag","Exception",e)
            e.printStackTrace()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                try {
                    val builder = AlertDialog.Builder(this@RegistrationActivity)
                    builder.setTitle(resources.getString(R.string.exit))
                        .setMessage(getString(R.string.are_you_sure_you_want_to_exit_this_screen))
                        .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                            // If "Yes" is clicked, exit the app
                            finish()
                        }
                        .setNegativeButton(resources.getString(R.string.no), null) // If "No" is clicked, do nothing
                        .show()
                } catch (e: Exception) {
                    Log.d("mytag","Exception",e)
                    e.printStackTrace()
                }
            }
        })
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestThePermissions()
                return

            }
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                locationListener
            )
        } catch (e: Exception) {
            Log.d("mytag","Exception",e)
            e.printStackTrace()
        }

    }
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if(!isInternetAvailable){
                latitude=location.latitude
                longitude=location.longitude
                Log.d("mytag","$latitude,$longitude")
                binding.etLocation.setText("$latitude,$longitude")
            }

        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}
    }
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
        private const val MIN_TIME_BW_UPDATES: Long = 1000 * 60 * 1 // 1 minute
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 10f // 10 meters
    }


    private fun saveUserDetails() {
        var gramPanchayatName=""
        if(binding.actVillage.text.toString().equals("Other")){
            gramPanchayatName=binding.etGramPanchayatName.text.toString()
        }else{
            gramPanchayatName=binding.actVillage.text.toString()
        }
        val user=User(
            fullName = binding.etFullName.text.toString(),
            district = districtId,
            taluka = talukaId,
            village = villageId,
            mobile = binding.etMobileNumber.text.toString(),
            aadharCardId = binding.etAadharCard.text.toString(),
            latitude = latitude.toString(),
            longitude = longitude.toString(),
            aadharIdCardPhoto = aadharIdImagePath,
            gramsevakIdCardPhoto = gramsevakIdImagePath,
            tabletImeiPhoto = tabletImeiPhotoPath,
            beneficaryPhoto = photoImagePath,
            isSynced = false,
            grampanchayatName = gramPanchayatName
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var rows: Long=0
                val waitJob=async { rows=userDao.insertUser(user) }
                waitJob.await()
                if(rows>0){
                    runOnUiThread {
                        val toast=Toast.makeText(this@RegistrationActivity,
                            getString(
                                R.string.user_added_successfully
                            ),Toast.LENGTH_SHORT)
                        toast.show()
                    }
                    val intent= Intent(this@RegistrationActivity, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                }else{
                    runOnUiThread {
                        val toast=Toast.makeText(this@RegistrationActivity,
                            getString(
                                R.string.user_not_added_please_try_again
                            ),Toast.LENGTH_SHORT)
                        toast.show()
                    }
                }
                Log.d("mytag","Rows Inserted : $rows")
            } catch (e: Exception) {
                Log.d("mytag","Exception Inserted : ${e.message}")
                e.printStackTrace()
            }
        }

    }


    fun splitStringByHalf(input: String): Pair<String, String> {
        val length = input.length
        val halfLength = length / 2
        val firstHalf = input.substring(0, halfLength)
        val secondHalf = input.substring(halfLength)
        return Pair(firstHalf, secondHalf)
    }
    override fun onResume() {
        super.onResume()
        if (!isLocationEnabled()) {
            showEnableLocationDialog()
            getTheLocation()
        } else {
            requestLocationUpdates()
            getTheLocation()
        }
    }
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this@RegistrationActivity, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@RegistrationActivity, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permissions
           requestThePermissions()
            return
        }
        Log.d("mytag", "requestLocationUpdates() s")
        // Request last known location
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                latitude
            } ?: run {
                // Handle case where location is null
//                Toast.makeText(
//                    this@RegistrationActivity, "Unable to retrieve location", Toast.LENGTH_LONG
//                ).show()
            }
        }
    }
    private fun showEnableLocationDialog() {
        val builder = AlertDialog.Builder(this@RegistrationActivity)
        builder.setMessage(getString(R.string.app_requires_location_enabled_please_enabled_location))
            .setTitle(resources.getString(R.string.enable_location))
            .setIcon(R.drawable.ic_location_colored)
            .setCancelable(false).setPositiveButton(resources.getString(R.string.enable)) { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(resources.getString(R.string.no)){dialog,_ ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }
    private fun getTheLocation() {
        Log.d("mytag","getTheLocation")
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
               // requestThePermissions()
                return
            }
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        latitude=it.latitude
                        longitude=it.longitude
                        addressFromLatLong=getAddressFromLatLong()
                        binding.etLocation.setText("${it.latitude},${it.longitude}")
                    } ?: run {
                        Toast.makeText(
                            this@RegistrationActivity,
                            "Unable to retrieve location",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } catch (e: Exception) {

        }
    }
    private fun initializeFields() {

        try {
            talukaList=ArrayList<AreaItem>()
            villageList=ArrayList<AreaItem>()
            val districtAdapter = ArrayAdapter(
                this, android.R.layout.simple_list_item_1, districtNames
            )
            binding.actDistrict.setAdapter(districtAdapter)
            binding.actDistrict.setOnItemClickListener { parent, view, position, id ->
                districtId=districtList[position].location_id
                binding.actTaluka.setText("")
                binding.actVillage.setText("")
                CoroutineScope(Dispatchers.IO).launch {
                    talukaNames.clear();
                    talukaList=areaDao.getAllTalukas(districtList[position].location_id)
                    for (taluka in talukaList){
                        talukaNames.add(taluka.name)
                    }
                    val talukaAdapter = ArrayAdapter(
                        this@RegistrationActivity, android.R.layout.simple_list_item_1, talukaNames
                    )
                    withContext(Dispatchers.Main){
                        binding.actTaluka.setAdapter(talukaAdapter)
                    }
                }
            }
            binding.actTaluka.setOnItemClickListener { parent, view, position, id ->
                CoroutineScope(Dispatchers.IO).launch {
                    talukaId=talukaList[position].location_id
                    villageNames.clear();
                    binding.actVillage.setText("")
                    villageList=areaDao.getVillageByTaluka(talukaList[position].location_id)
                    for (village in villageList){
                        villageNames.add(village.name)
                    }
                    villageNames.add("Other")
                    val villageAdapter = ArrayAdapter(
                        this@RegistrationActivity, android.R.layout.simple_list_item_1, villageNames
                    )
                    Log.d("mytag",""+villageNames.size)
                    withContext(Dispatchers.Main){
                        binding.actVillage.setAdapter(villageAdapter)
                        binding.actVillage.setOnFocusChangeListener { abaad, asd ->
                            binding.actVillage.showDropDown()
                        }
                        binding.actVillage.setOnClickListener {
                            binding.actVillage.showDropDown()
                        }
                    }
                }
            }
            binding.actVillage.setOnItemClickListener { parent, view, position, id ->
                if(binding.actVillage.text.toString().equals("Other")){
                    villageId="999999"
                    binding.etGramPanchayatName.visibility=View.VISIBLE;
                }else{
                    binding.etGramPanchayatName.visibility=View.GONE;
                    villageId=villageList[position].location_id
                }

            }
            binding.actDistrict.setOnFocusChangeListener { abaad, asd ->
                binding.actDistrict.showDropDown()
            }
            binding.actDistrict.setOnClickListener {
                binding.actDistrict.showDropDown()
            }
            binding.actTaluka.setOnFocusChangeListener { abaad, asd ->
                binding.actTaluka.showDropDown()
            }
            binding.actTaluka.setOnClickListener {
                binding.actTaluka.showDropDown()
            }

            binding.etAadharCard.addTextChangedListener(object :TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    if(s?.length==12){

                       CoroutineScope(Dispatchers.IO).launch {
                           if(isInternetAvailable)
                           {
                               checkIfAadharCardExists(s.toString())
                           }

                       }

                    }
                }
            })
        } catch (e: Exception) {
            Log.d("mytag","MainActivity ${e.message}",e)
        }

    }
    private fun getAddressFromLatLong():String{
        val geocoder: Geocoder
        try {
            val addresses: List<Address>?
            geocoder = Geocoder(this, Locale.getDefault())
            addresses = geocoder.getFromLocation(
                latitude, longitude,
                1) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            var fullAddress=""
            if (addresses != null) {
                if(addresses.size>0){
                    fullAddress= addresses!![0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                   /* val city: String = addresses!![0].locality
                    val state: String = addresses!![0].adminArea
                    val country: String = addresses!![0].countryName
                    val postalCode: String = addresses!![0].postalCode
                    val knownName: String = addresses!![0].featureName*/

                }
            }
            return fullAddress
        } catch (e: Exception) {
            return ""
        }

    }
    private fun validateFieldsX(): Boolean {
        val validationResults = mutableListOf<Boolean>()
        // Full Name
        if (MyValidator.isValidName(binding.etFullName.text.toString())) {
            binding.etFullName.error = null
            validationResults.add(true)
        } else {
            binding.etFullName.error = resources.getString(R.string.full_name_required)
            validationResults.add(false)
        }
        // DOB
        if(binding.etGramPanchayatName.isVisible)
        {
            if (binding.etGramPanchayatName.text.toString().length > 0 && !binding.etGramPanchayatName.text.isNullOrBlank()) {
                binding.etGramPanchayatName.error = null
                validationResults.add(true)
            } else {
                binding.etGramPanchayatName.error = resources.getString(R.string.enter_grampanchayat_name)
                validationResults.add(false)
            }
        }

        // District
        if (binding.actDistrict.enoughToFilter()) {
            binding.actDistrict.error = null
            validationResults.add(true)
        } else {
            binding.actDistrict.error = resources.getString(R.string.select_district)
            validationResults.add(false)
        }
        // Taluka

        if (binding.actTaluka.enoughToFilter()) {
            binding.actTaluka.error = null
            validationResults.add(true)
        } else {
            binding.actTaluka.error = resources.getString(R.string.select_taluka)
            validationResults.add(false)
        }
        // Village
        if (binding.actVillage.enoughToFilter()) {
            binding.actVillage.error = null
            validationResults.add(true)
        } else {
            binding.actVillage.error = resources.getString(R.string.select_village)
            validationResults.add(false)
        }
        // Mobile
        if (MyValidator.isValidMobileNumber(binding.etMobileNumber.text.toString())) {
            binding.etMobileNumber.error = null
            validationResults.add(true)
        } else {
            binding.etMobileNumber.error = resources.getString(R.string.enter_valid_mobile)
            validationResults.add(false)
        }
        if (binding.etAadharCard.text.toString().length > 0 && !binding.etAadharCard.text.isNullOrBlank() && binding.etAadharCard.text.length==12) {
            binding.etAadharCard.error = null
            validationResults.add(true)
        } else {
            binding.etAadharCard.error =
                resources.getString(R.string.enter_aadhar_card_number)
            validationResults.add(false)
        }
        if (aadharIdImagePath.toString().length > 0) {
            validationResults.add(true)
        } else {
            validationResults.add(false)
        }
        if (gramsevakIdImagePath.toString().length > 0) {
            validationResults.add(true)
        } else {
            validationResults.add(false)

        }
        if (photoImagePath.toString().length > 0) {
            validationResults.add(true)
        } else {
            validationResults.add(false)
        }
        if (tabletImeiPhotoPath.toString().length > 0) {
            validationResults.add(true)
        } else {
            validationResults.add(false)
        }
        if(binding.etLocation.text.toString().length<1){

            getTheLocation()
        }

        return !validationResults.contains(false)
    }
    private var isPermissionRationaleShown = false

    private fun requestThePermissions() {

        Log.d("mytag","requestThePermissions()")
        try {
            PermissionX.init(this@RegistrationActivity)
                .permissions(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.CAMERA
                )
                .onForwardToSettings { scope, deniedList ->
                    scope.showForwardToSettingsDialog(deniedList, "You need to allow necessary permissions in Settings manually", "OK", "Cancel")
                }
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        getTheLocation()
                    } else {
                        Toast.makeText(
                            this,
                            "These permissions are denied: $deniedList",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        } catch (e: Exception) {
            Log.d("mytag","Exception ",e)
            e.printStackTrace()
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId==android.R.id.home){
            val builder = AlertDialog.Builder(this@RegistrationActivity)
            builder.setTitle("Exit")
                .setMessage("Are you sure you want to exit this screen?")
                .setPositiveButton("Yes") { _, _ ->
                    // If "Yes" is clicked, exit the app
                    finish()
                }
                .setNegativeButton("No", null) // If "No" is clicked, do nothing
                .show()
        }
        return super.onOptionsItemSelected(item)
    }

    private suspend fun checkIfAadharCardExists(aadharCardNumber:String):Boolean{

        return suspendCancellableCoroutine { continuation->
            runOnUiThread {
                dialog.show()
            }
            val apiService = ApiClient.create(this@RegistrationActivity)
            CoroutineScope(Dispatchers.IO).launch {
                try {

                    val response= apiService.checkIfAadharExists(aadharCardNumber)
                    if(response.isSuccessful){
                        runOnUiThread { dialog.dismiss() }
                        if(response.body()?.status.equals("false"))
                        {
                            continuation.resume(true)
                            isAadharVerified=true
                            runOnUiThread { binding.etAadharCard.error=null }
                            withContext(Dispatchers.Main){
                            }

                        }else{
                            continuation.resume(false)
                            runOnUiThread {
                                binding.etAadharCard.error="Aadhar number already registered with another user"
                            }
                            withContext(Dispatchers.Main){
                                Toast.makeText(this@RegistrationActivity,response.body()?.message,
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }else{
                        continuation.resume(false)
                        withContext(Dispatchers.Main){
                            dialog.dismiss()
                            Toast.makeText(this@RegistrationActivity,resources.getString(R.string.failed_updating_labour_response),
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    //runOnUiThread {dialog.dismiss()  }
                } catch (e: Exception) {
                    continuation.resume(false)
                    runOnUiThread { dialog.dismiss() }
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@RegistrationActivity,resources.getString(R.string.response_failed),
                            Toast.LENGTH_SHORT).show()
                    }
                    Log.d("mytag","checkIfAadharCardExists "+e.message)
                }
            }
        }

    }
    suspend fun createTempJpgFile(context: Context, byteArray: ByteArray, filename: String): File? =
        withContext(Dispatchers.IO) {
            var tempFile: File? = null
            try {
                // Create a temporary file
                tempFile = File.createTempFile(filename, ".jpg", context.cacheDir)

                // Write the byte array to the temporary file
                val outputStream = FileOutputStream(tempFile)
                outputStream.write(byteArray)
                outputStream.close()
            } catch (e: IOException) {
                // Handle exceptions, such as if the file cannot be created or if there's an I/O error
                e.printStackTrace()
                Log.d("mytag", "Exception " + e.message)
            }
            tempFile // Return the temporary file (or null if an error occurred)
        }

    fun startCameraActivity(requestCode: Int) {
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra("requestCode", requestCode)
        startForResult.launch(intent)
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val requestCode = result.data?.getIntExtra("requestCode", -1)
        if (result.resultCode == Activity.RESULT_OK) {
            val capturedImageUri = result.data?.getParcelableExtra<Uri>("capturedImageUri")
            val requestCode = result.data?.getIntExtra("requestCode", -1)
            if (capturedImageUri != null && requestCode != -1) {
                // Image captured successfully, do something with the URI

                when (requestCode) {
                    REQUEST_CODE_PHOTO -> {
                        Glide.with(this@RegistrationActivity).load(capturedImageUri).
                        override(150,120).into(binding.ivPhoto)
                        photoImagePath= capturedImageUri.toString()

                        CoroutineScope(Dispatchers.IO).launch {
                            runOnUiThread {  dialog.show() }
                            val beneficiaryPhotoJob=async {val uri=uriStringToBitmap(this@RegistrationActivity,capturedImageUri.toString(),"$latitude,$longitude",addressFromLatLong)!! }
                            beneficiaryPhotoJob.await()
                            withContext(Dispatchers.Main){
                                dialog.dismiss()
                            }
0
                        }

                    }
                    REQUEST_CODE_TABLET_IMEI -> {
                        Glide.with(this@RegistrationActivity).load(capturedImageUri).
                        override(150,120).into(binding.ivTabletImei)
                        tabletImeiPhotoPath= capturedImageUri.toString()
                        CoroutineScope(Dispatchers.IO).launch {
                            runOnUiThread {  dialog.show() }
                            val imeiPhotoJob= async {
                                val uri=uriStringToBitmap(this@RegistrationActivity,capturedImageUri.toString(),"$latitude,$longitude",addressFromLatLong)!! }
                            imeiPhotoJob.await()
                            withContext(Dispatchers.Main){

                                    dialog.dismiss()


                            }
                        }

                    }
                    REQUEST_CODE_AADHAR_CARD -> {
                        Glide.with(this@RegistrationActivity).load(capturedImageUri).
                        override(150,120).into(binding.ivAadhar)
                        aadharIdImagePath= capturedImageUri.toString()
                        CoroutineScope(Dispatchers.IO).launch {
                            runOnUiThread {  dialog.show() }
                            val aadharCardPhotoJob=async { val uri=uriStringToBitmap(this@RegistrationActivity,capturedImageUri.toString(),"$latitude,$longitude",addressFromLatLong)!! }
                            aadharCardPhotoJob.await()
                            withContext(Dispatchers.Main){
                                dialog.dismiss()
                            }

                        }
                    }
                    REQUEST_CODE_GRAMSEVAK -> {
                        Glide.with(this@RegistrationActivity).load(capturedImageUri).
                        override(150,120).into(binding.ivGramsevakId)
                        gramsevakIdImagePath= capturedImageUri.toString()
                        CoroutineScope(Dispatchers.IO).launch {
                            runOnUiThread {  dialog.show() }
                            val gramsevakPhotoJob=async { val uri=uriStringToBitmap(this@RegistrationActivity,capturedImageUri.toString(),"$latitude,$longitude",addressFromLatLong)!! }
                            gramsevakPhotoJob.await()
                            withContext(Dispatchers.Main){
                                dialog.dismiss()
                            }

                        }
                    }
                    else -> {
                        Toast.makeText(this@RegistrationActivity,resources.getString(R.string.unknown_request_code),Toast.LENGTH_SHORT).show()
                    }
                }

            } else {

                Toast.makeText(this@RegistrationActivity,resources.getString(R.string.image_capture_failed),Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == Activity.RESULT_CANCELED) {
            val requestCode = result.data?.getIntExtra("requestCode", -1)
            if (requestCode != -1) {
               Toast.makeText(this@RegistrationActivity,resources.getString(R.string.image_capture_failed),Toast.LENGTH_SHORT).show()
            }
        }
    }

    suspend fun uriStringToBitmap(context: Context, uriString: String, latlongtext: String,addressText: String): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(uriString)
                val futureTarget = Glide.with(context)
                    .asBitmap()
                    .load(uriString)
                    .submit()
                val bitmap = futureTarget.get()

                // Add text overlay to the bitmap
                val canvas = Canvas(bitmap)
                val paint = Paint().apply {
                    color = Color.RED
                    textSize = 50f // Text size in pixels
                    isAntiAlias = true
                    style = Paint.Style.FILL
                }
                val currentDateTime = Date()
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val formattedDateTime = formatter.format(currentDateTime)
                val x = 50f // Adjust the x-coordinate as needed
                val y = bitmap.height.toFloat() - 50f // Adjust the y-coordinate as needed
                val xAddress = 50f // Adjust the x-coordinate as needed
                val yAddress = bitmap.height.toFloat() - 100f
                canvas.drawText(latlongtext, x, y, paint)
                val addressTextWidth = paint.measureText(addressText)
                val availableWidth = bitmap.width.toFloat() - xAddress // A
                if(addressTextWidth > availableWidth){
                    val (firstHalf, secondHalf) = splitStringByHalf(addressText)
                    canvas.drawText(firstHalf, xAddress, yAddress-50, paint)
                    canvas.drawText(secondHalf, xAddress, yAddress, paint)
                    canvas.drawText(formattedDateTime, xAddress, yAddress-100, paint)
                }else{
                    canvas.drawText(addressText, xAddress, yAddress, paint)
                    canvas.drawText(formattedDateTime, xAddress, yAddress-50, paint)
                }

                // Save the modified bitmap back to the same location
                saveBitmapToFile(context, bitmap, uri)

                uri // Return the URI of the modified bitmap
            } catch (e: Exception) {
                Log.d("mytag","Exception => "+e.message)
                Log.d("mytag","Exception => ${e.message}",e)
                e.printStackTrace()
                null
            }
        }
    }
    private suspend fun saveBitmapToFile(context: Context, bitmap: Bitmap, uri: Uri) {
        try {
            val outputStream = context.contentResolver.openOutputStream(uri)
            outputStream?.let { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
            outputStream?.flush()
            outputStream?.close()
            val imageFile=bitmapToFile(context,bitmap)
            val compressedImageFile = imageFile?.let {
                Compressor.compress(context, it) {
                    format(Bitmap.CompressFormat.JPEG)
                    resolution(780,1360)
                    quality(100)
                    size(500000) // 500 KB
                }
            }
            compressedImageFile?.let { compressedFile:File ->
                try {
                    val inputStream = FileInputStream(compressedFile)
                    val outputStream = context.contentResolver.openOutputStream(uri)
                    inputStream.use { input ->
                        outputStream?.use { output ->
                            input.copyTo(output)
                        }
                    }
                } catch (e: IOException) {
                    // Handle exception
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("mytag","Exception => ${e.message}",e)
        }
    }
    fun bitmapToFile(context: Context, bitmap: Bitmap): File? {
        // Create a file in the cache directory
        val time= Calendar.getInstance().timeInMillis.toString()
        val file = File(context.cacheDir, time)

        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            return file
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("mytag","Exception => ${e.message}",e)
        }
        return null
    }


    private fun showPermissionDeniedDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
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