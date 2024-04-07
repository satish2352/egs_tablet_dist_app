package com.sumagoinfotech.egsregistration.ui

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.permissionx.guolindev.PermissionX
import com.sumagoinfotech.egsregistration.R
import com.sumagoinfotech.egsregistration.database.AppDatabase
import com.sumagoinfotech.egsregistration.database.AreaDao
import com.sumagoinfotech.egsregistration.database.AreaItem
import com.sumagoinfotech.egsregistration.databinding.ActivityRegistrationBinding
import com.sumagoinfotech.egsregistration.utils.CustomProgressDialog
import com.sumagoinfotech.egsregistration.utils.MyValidator
import com.sumagoinfotech.egsregistration.webservice.ApiClient
import com.sumagoinfotech.egsregistration.webservice.FileInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RegistrationActivity : AppCompatActivity() {
    private  var isInternetAvailable=false
    private  var latitude:Double=0.0
    private  var longitude:Double=0.0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var appDatabase: AppDatabase
    private lateinit var areaDao: AreaDao
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
    private  var gramsevakId:String=""
    private  var aadharIdImagePath:String=""
    private  var photoImagePath:String=""
    private  var gramsevakIdImagePath:String=""
    private  var tabletImeiPhotoPath:String=""
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var dialog: CustomProgressDialog
    private val uriMap = mutableMapOf<Int, Uri>()
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
        supportActionBar?.title=resources.getString(R.string.egs)
        appDatabase=AppDatabase.getDatabase(this)
        areaDao=appDatabase.areaDao()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        try {
                cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                    if (success) {
                        Log.d("mytag", "Success => ${uriMap.values}")
                        // Retrieve URI for Aadhar Card
                        val uriAadhar = uriMap[REQUEST_CODE_AADHAR_CARD]
                        if (uriAadhar != null) {
                            Log.d("mytag", "URI for Aadhar Card: $uriAadhar")
                            binding.ivAadhar.setImageURI(uriAadhar)
                            aadharIdImagePath= uriAadhar.toString()
                            CoroutineScope(Dispatchers.IO).launch {
                                val uri=uriStringToBitmap(this@RegistrationActivity,uriAadhar.toString(),binding.etLocation.text.toString(),addressFromLatLong)
                                withContext(Dispatchers.Main){
                                    // binding.ivPhoto.setImageBitmap(bitmap)
                                }

                            }
                        } else {
                            Log.d("mytag", "URI for Aadhar Card is null")
                        }
                        // Retrieve URI for MGNREGA Card
                        val uriGramsevakId = uriMap[REQUEST_CODE_GRAMSEVAK]
                        if (uriGramsevakId != null) {
                            Log.d("mytag", "URI for MGNREGA Card: $uriGramsevakId")
                            binding.ivGramsevakId.setImageURI(uriGramsevakId)
                            gramsevakIdImagePath= uriGramsevakId.toString()
                            CoroutineScope(Dispatchers.IO).launch {
                                val uri=uriStringToBitmap(this@RegistrationActivity,uriGramsevakId.toString(),binding.etLocation.text.toString(),addressFromLatLong)
                                try {
                                    getAddressFromLatLong()
                                } finally {

                                }
                                withContext(Dispatchers.Main){
                                    // binding.ivPhoto.setImageBitmap(bitmap)
                                }

                            }
                        } else {
                            Log.d("mytag", "URI for MGNREGA Card is null")
                        }
                        // Retrieve URI for Photo
                        val uriPhoto = uriMap[REQUEST_CODE_PHOTO]
                        if (uriPhoto != null) {
                            Log.d("mytag", "URI for Photo: $uriPhoto")
                            binding.ivPhoto.setImageURI(uriPhoto)
                            photoImagePath= uriPhoto.toString()
                            CoroutineScope(Dispatchers.IO).launch {
                                val uri=uriStringToBitmap(this@RegistrationActivity,uriPhoto.toString(),binding.etLocation.text.toString(),addressFromLatLong)
                                withContext(Dispatchers.Main){
                                    // binding.ivPhoto.setImageBitmap(bitmap)
                                }
                            }
                        } else {
                            Log.d("mytag", "URI for Photo is null")
                        }
                        val uriTabletImei = uriMap[REQUEST_CODE_TABLET_IMEI]
                        if (uriTabletImei != null) {
                            Log.d("mytag", "URI for Photo: $uriTabletImei")
                            binding.ivTabletImei.setImageURI(uriTabletImei)
                            tabletImeiPhotoPath= uriTabletImei.toString()
                            CoroutineScope(Dispatchers.IO).launch {
                                val uri=uriStringToBitmap(this@RegistrationActivity,uriTabletImei.toString(),binding.etLocation.text.toString(),addressFromLatLong)
                                withContext(Dispatchers.Main){
                                    // binding.ivPhoto.setImageBitmap(bitmap)
                                }
                            }
                        } else {
                            Log.d("mytag", "URI for Photo is null")
                        }
                    } else {
                        // Image capture failed or was canceled
                        Log.d("mytag", "Failed")
                    }
                }
        } catch (e: Exception) {
            Log.d("mytag","cameraLauncher=>registerForActivityException=>"+e.message)
            e.printStackTrace()
        }
        binding.btnRegister.setOnClickListener {
          if(isInternetAvailable){
              if(validateFieldsX()){
                  if(validateDocuments()){

                     CoroutineScope(Dispatchers.Default).launch {
                         uploadLabourOnline()
                     }
                  }else{
                      Toast.makeText(this@RegistrationActivity,resources.getString(R.string.select_all_documents),Toast.LENGTH_LONG).show()
                  }
              }else{
                  Toast.makeText(this@RegistrationActivity,resources.getString(R.string.enter_all_details),Toast.LENGTH_LONG).show()
              }
          }else{
              Toast.makeText(this@RegistrationActivity,resources.getString(R.string.check_internet_connection),Toast.LENGTH_LONG).show()
          }
        }
        binding.ivChangeAadhar.setOnClickListener {
            captureImage(REQUEST_CODE_AADHAR_CARD)
        }
        binding.ivChangePhoto.setOnClickListener {
            captureImage(REQUEST_CODE_PHOTO)
        }
        binding.ivChangeGramsevakId.setOnClickListener {
            captureImage(REQUEST_CODE_GRAMSEVAK)
        }
        binding.ivChangeTabletImei.setOnClickListener {
            captureImage(REQUEST_CODE_TABLET_IMEI)
        }
        CoroutineScope(Dispatchers.IO).launch {
            districtList=areaDao.getAllDistrict()
            Log.d("mytag",districtList.size.toString())
            withContext(Dispatchers.Main){
                for (district in districtList){
                    districtNames.add(district.name)
                }
                initializeFields()
            }

        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

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
        })

    }
    suspend fun uriStringToBitmap(context: Context, uriString: String, text: String, addressText: String): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(uriString)
                val futureTarget = Glide.with(context)
                    .asBitmap()
                    .load(uri)
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
                canvas.drawText(text, x, y, paint)
                canvas.drawText(addressText, xAddress, yAddress, paint)
                canvas.drawText(formattedDateTime, xAddress, yAddress-50, paint)

                // Save the modified bitmap back to the same location
                saveBitmapToFile(context, bitmap, uri)

                uri // Return the URI of the modified bitmap
            } catch (e: Exception) {
                Log.d("mytag","Exception => "+e.message)
                e.printStackTrace()
                null
            }
        }
    }
    private fun saveBitmapToFile(context: Context, bitmap: Bitmap, uri: Uri) {
        try {
            val outputStream = context.contentResolver.openOutputStream(uri)
            outputStream?.let { bitmap.compress(Bitmap.CompressFormat.JPEG, 10, it) }
            outputStream?.flush()
            outputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    override fun onResume() {
        super.onResume()
        //checkAndPromptGps()
        requestThePermissions()
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
            ActivityCompat.requestPermissions(
                this@RegistrationActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000
            )
            Log.d("mytag", "requestLocationUpdates()  return ")
            return
        }
        Log.d("mytag", "requestLocationUpdates() ")

        // Request last known location
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                latitude
            } ?: run {
                // Handle case where location is null
                Toast.makeText(
                    this@RegistrationActivity, "Unable to retrieve location", Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    private fun checkAndPromptGps() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // GPS is not enabled, prompt the user to enable it
            AlertDialog.Builder(this)
                .setMessage(" Please enable GPS on your device")
                .setPositiveButton("Yes") { dialog, _ ->
                    // Open the location settings to enable GPS
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                    // Handle the case when the user chooses not to enable GPS
                }
                .show()
        }
    }

    private fun showEnableLocationDialog() {
        val builder = AlertDialog.Builder(this@RegistrationActivity)
        builder.setMessage("Location services are disabled. Do you want to enable them?")
            .setCancelable(false).setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                // Handle the case when the user refuses to enable location services
                Toast.makeText(
                    this@RegistrationActivity,
                    "Unable to retrieve location without enabling location services",
                    Toast.LENGTH_LONG
                ).show()
            }
        val alert = builder.create()
        alert.show()
    }
    private fun getTheLocation() {

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
            villageId=villageList[position].location_id
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

                    val city: String = addresses!![0].locality
                    val state: String = addresses!![0].adminArea
                    val country: String = addresses!![0].countryName
                    val postalCode: String = addresses!![0].postalCode
                    val knownName: String = addresses!![0].featureName
                    Log.d("mytag",fullAddress)
                    Log.d("mytag",city)
                    Log.d("mytag",state)
                    Log.d("mytag",country)
                    Log.d("mytag",postalCode)
                    Log.d("mytag",knownName)
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
        if (binding.etGramPanchayatName.text.toString().length > 0 && !binding.etGramPanchayatName.text.isNullOrBlank()) {
            binding.etGramPanchayatName.error = null
            validationResults.add(true)
        } else {
            binding.etGramPanchayatName.error = resources.getString(R.string.enter_grampanchayat_name)
            validationResults.add(false)
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
        return !validationResults.contains(false)
    }
    private fun validateDocuments(): Boolean {
        val validationResults = mutableListOf<Boolean>()
        // Full Name

        if (aadharIdImagePath.toString().length > 0 && !aadharIdImagePath.isNullOrBlank()) {
            validationResults.add(true)
        } else {
            validationResults.add(false)
        }
        if (gramsevakIdImagePath.toString().length > 0 && !gramsevakIdImagePath.isNullOrBlank()) {
            validationResults.add(true)
        } else {
            validationResults.add(false)
        }
        if (photoImagePath.toString().length > 0 && !photoImagePath.isNullOrBlank()) {
            validationResults.add(true)
        } else {
            validationResults.add(false)
        }
        if (tabletImeiPhotoPath.toString().length > 0 && !tabletImeiPhotoPath.isNullOrBlank()) {
            validationResults.add(true)
        } else {
            validationResults.add(false)
        }
        return !validationResults.contains(false)
    }
    private fun requestThePermissions() {

        try {
            PermissionX.init(this@RegistrationActivity)
                .permissions(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.CAMERA
                )
                .onExplainRequestReason { scope, deniedList ->
                    scope.showRequestReasonDialog(
                        deniedList,
                        "Core fundamental are based on these permissions",
                        "OK",
                        "Cancel"
                    )
                }
                .onForwardToSettings { scope, deniedList ->
                    scope.showForwardToSettingsDialog(
                        deniedList,
                        "You need to allow necessary permissions in Settings manually",
                        "OK",
                        "Cancel"
                    )
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
        }
    }
    private fun captureImage(requestCode: Int) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "image_$timestamp"
            val mediaStorageDir = File(externalMediaDirs[0], "myfiles")
            val uriFolder = Uri.parse(mediaStorageDir.absolutePath)
            val myAppFolder = File(uriFolder.toString())

            // Create the folder if it doesn't exist
            if (!myAppFolder.exists()) {
                myAppFolder.mkdirs()
            }

            // Create the file for the image
            val outputFile = File.createTempFile(fileName, ".jpg", myAppFolder)
            val uri = FileProvider.getUriForFile(this, "com.sumagoinfotech.egsregistration.provider", outputFile)

            // Store the URI in the map with the corresponding request code
            uriMap[requestCode] = uri

            // Launch the camera to capture the image
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Log.d("mytag","captureImage=>Exception=>"+e.message)
            e.printStackTrace()
        }
    }
    suspend fun uriToFile(context: Context, uri: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val requestOptions = RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // Don't cache to avoid reading from cache
                    .skipMemoryCache(false) // Skip memory cache
                val bitmap = Glide.with(context)
                    .asBitmap()
                    .load(uri)
                    .apply(requestOptions)
                    .submit()
                    .get()
                val time= Calendar.getInstance().timeInMillis.toString()
                // Create a temporary file to store the bitmap
                val file = File(context.cacheDir, "$time.jpg")
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 10, outputStream)
                outputStream.flush()
                outputStream.close()

                file // Return the temporary file
            } catch (e: Exception) {
                Log.d("mytag", "Exception uriToFile: ${e.message}")
                null // Return null if there's an error
            }
        }
    }
    private suspend fun createFilePart(fileInfo: FileInfo): MultipartBody.Part? {
        try {
            Log.d("mytag",""+fileInfo.fileUri)
            val file: File? = uriToFile(applicationContext, fileInfo.fileUri)
            return file?.let {
                val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), it)
                MultipartBody.Part.createFormData(fileInfo.fileName, it.name, requestFile)
            }
        } catch (e: Exception) {
            Log.d("mytag", "Exception createFilePart: ${e.message}")
            e.printStackTrace()
           return null
        }
    }
    private suspend fun uploadLabourOnline(){
        runOnUiThread {
            dialog.show()
        }
        val apiService = ApiClient.create(this@RegistrationActivity)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val aadharCardImage =
                    createFilePart(FileInfo("aadhar_image", aadharIdImagePath))
                val gramsevakIdImage =
                    createFilePart(FileInfo("gram_sevak_id_card_photo", gramsevakIdImagePath))
                val profileImage =
                    createFilePart(FileInfo("photo_of_beneficiary", photoImagePath))
                val tabletImeiImage =
                    createFilePart(FileInfo("photo_of_tablet_imei", tabletImeiPhotoPath))
                val response= apiService.uploadLaborInfo(
                    fullName = binding.etFullName.text.toString(),
                    grampanchayatName = binding.etGramPanchayatName.text.toString(),
                    aadharNumber = binding.etAadharCard.text.toString(),
                    districtId=districtId,
                    talukaId =talukaId,
                    villageId = villageId,
                    mobileNumber = binding.etMobileNumber.text.toString(),
                    latitude=latitude.toString(),
                    longitude = longitude.toString(),
                    aadharFile = aadharCardImage!!,
                    gramsevakIdFile = gramsevakIdImage!!,
                    photoFile = profileImage!!,
                    tabletImeiFile = tabletImeiImage!!,
                )
                if(response.isSuccessful){
                    if(response.body()?.status.equals("True"))
                    {
                        withContext(Dispatchers.Main){
                            Toast.makeText(this@RegistrationActivity,response.body()?.message,
                                Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }else{
                        withContext(Dispatchers.Main){
                            Toast.makeText(this@RegistrationActivity,resources.getString(R.string.failed_updating_labour),
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    Log.d("mytag",""+response.body()?.message)
                    Log.d("mytag",""+response.body()?.status)
                }else{
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@RegistrationActivity,resources.getString(R.string.failed_updating_labour_response),
                            Toast.LENGTH_SHORT).show()
                    }
                }
                runOnUiThread {dialog.dismiss()  }
            } catch (e: Exception) {
                runOnUiThread { dialog.dismiss() }
                withContext(Dispatchers.Main){
                    Toast.makeText(this@RegistrationActivity,resources.getString(R.string.failed_updating_labour_response),
                        Toast.LENGTH_SHORT).show()
                }
                Log.d("mytag","uploadLabourOnline "+e.message)
            }
        }
    }
    private fun deleteFileFromUri(uri: Uri) {
        val contentResolver: ContentResolver = applicationContext.contentResolver
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        var filePath: String? = null

        // Query the file path from the MediaStore
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            cursor.moveToFirst()
            filePath = cursor.getString(columnIndex)
        }

        filePath?.let { path ->
            // Delete the file
            val fileToDelete = File(path)
            if (fileToDelete.exists()) {
                if (fileToDelete.delete()) {
                    // File deleted successfully
                    Toast.makeText(this, "File deleted", Toast.LENGTH_SHORT).show()
                } else {
                    // Failed to delete the file
                    Toast.makeText(this, "Failed to delete the file", Toast.LENGTH_SHORT).show()
                }
            } else {
                // File doesn't exist
                Toast.makeText(this, "File doesn't exist", Toast.LENGTH_SHORT).show()
            }
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

}