package com.sipl.egstabdistribution.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sipl.egstabdistribution.R
import com.sipl.egstabdistribution.databinding.ActivityCameraBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraActivity : AppCompatActivity() {
    private var imageCapture: ImageCapture? = null
    private lateinit var binding:ActivityCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val requestCode = intent.getIntExtra("requestCode", -1)
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            // Request camera permissions
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Set up the capture button click listener
        binding.btnCapture.setOnClickListener { takePhoto(requestCode) }

        binding.btnRetake.setOnClickListener {
            binding.btnCapture.isEnabled = true
            binding.btnRetake.visibility = View.GONE
            binding.btnOkay.visibility= View.GONE
            binding.viewFinder.visibility= View.VISIBLE
            binding.ivPreview.visibility= View.GONE
            binding.ivPreview.setImageDrawable(null)
            startCamera()
        }

        // Set up the cancel button click listener
        binding.btnCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
        binding.btnOkay.setOnClickListener {
            finish()
        }
        binding.btnOkay.visibility= View.GONE

        binding.btnFlash.setOnClickListener {
            toggleFlash()
            Log.d("mytag","toggle")
        }

        binding.btnFlash.setBackgroundColor(Color.GRAY)


    }

    private fun toggleFlash() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val camera = cameraProvider.bindToLifecycle(this, cameraSelector)
            val cameraControl = camera.cameraControl

            val torchState = camera.cameraInfo.torchState.value ?: TorchState.OFF

            when (torchState) {
                TorchState.OFF -> { cameraControl.enableTorch(true)
                    binding.btnFlash.setBackgroundColor(Color.RED)
                }
                TorchState.ON -> { cameraControl.enableTorch(false)
                                    binding.btnFlash.setBackgroundColor(Color.GRAY)
                }
            }
        }, ContextCompat.getMainExecutor(this))
    }



    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Camera provider is now guaranteed to be available
            val cameraProvider = cameraProviderFuture.get()

            // Set up the preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // Set up the image capture use case
            imageCapture = ImageCapture.Builder()
                .build()

            // Select back camera as the default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind any previous use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto(requestCode:Int) {
        try {
            binding.viewFinder.visibility= View.VISIBLE

            val imageCapture = imageCapture ?: return
            val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
            val fileName = "image_$timestamp"
            val mediaStorageDir = File(externalMediaDirs[0], "myfiles")
            val uriFolder = Uri.parse(mediaStorageDir.absolutePath)
            val myAppFolder = File(uriFolder.toString())

            // Create the folder if it doesn't exist
            if (!myAppFolder.exists()) {
                myAppFolder.mkdirs()
            }
            val photoFile = File(
                myAppFolder,
                "$timestamp.jpg"
            )
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
            imageCapture.takePicture(
                outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                        // Pass back the request code indicating failure
                        setResult(Activity.RESULT_CANCELED, Intent().apply {
                            putExtra("requestCode", requestCode)
                        })
                        finish()
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        binding.ivPreview.setImageURI(output.savedUri)
                        val savedUri = Uri.fromFile(photoFile)
                        binding.btnRetake.visibility= View.VISIBLE
                        binding.btnOkay.visibility= View.VISIBLE


                        Log.d(TAG, "Photo capture succeeded: $savedUri")

                        // Pass back the request code indicating success
                        setResult(Activity.RESULT_OK, Intent().apply {
                            putExtra("requestCode", requestCode)
                            putExtra("capturedImageUri", savedUri)
                        })
                        binding.ivPreview.visibility = View.VISIBLE
                        binding.viewFinder.visibility= View.GONE

                    }
                })
        } catch (e: Exception) {
            Log.d("mytag","CameraActivity : ${e.message}",e)
            e.printStackTrace()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    companion object {
        private const val TAG = "mytag"
        private const val FILENAME_FORMAT = "yyyyMMddHHmmss"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
    }

    override fun onBackPressed() {

        if(binding.ivPreview.visibility== View.VISIBLE){
            binding.btnRetake.visibility = View.GONE
            binding.btnOkay.visibility= View.GONE
            binding.viewFinder.visibility= View.VISIBLE
            binding.ivPreview.visibility= View.GONE
        }else{
            super.onBackPressed()
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

    }

}
