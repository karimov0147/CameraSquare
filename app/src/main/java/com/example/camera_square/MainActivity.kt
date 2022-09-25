package com.example.camera_square

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns.MIME_TYPE
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.*
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import com.example.camera_square.databinding.ActivityMainBinding
import com.google.common.util.concurrent.ListenableFuture
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import java.io.File
import java.lang.Exception
import java.net.FileNameMap
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var cameraProcessFeature: ListenableFuture<ProcessCameraProvider>
    lateinit var binding : ActivityMainBinding
     var imageCapture: ImageCapture? = null
     var cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
     var flashMode = ImageCapture.FLASH_MODE_OFF



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val permissions =
            arrayOf<String>(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE )
        Permissions.check(
            this /*context*/,
            permissions,
            null /*rationale*/,
            null /*options*/,
            object : PermissionHandler() {
                override fun onGranted() {
                    initPreview()
                    // do your task.
                }
            })

        binding.cardView.setOnClickListener{
            imageCapture()
        }

        binding.cardView.setOnLongClickListener(object : View.OnLongClickListener{
            override fun onLongClick(v: View?): Boolean {
                var intent = Intent(this@MainActivity , MainActivity2::class.java)
                startActivity(intent )

                return false
            }

        })


        binding.reverseCamera.setOnClickListener{
            if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA){
                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                initPreview()
                binding.preview.animate()
                    .rotationYBy(180f)
                    .setDuration(1000)
//                    .withEndAction {
//                        binding.preview.animate()
//                            .rotationYBy(30f)
//                            .scaleX(1f)
//                            .scaleY(1f)
//                            .setDuration(166)
//                            .start()
//                    }
                    .start()
            }
            else{
                cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                initPreview()
                binding.preview.animate()
                    .rotationYBy(-180f)
                    .setDuration(1000)
                    .withEndAction {
//                        it.animate()
//                            .rotationYBy(90f)
//                            .setDuration(500)
//                            .start()
                    }
                    .start()
            }
        }

        binding.flashBtn.setOnClickListener{
         if (flashMode == FLASH_MODE_OFF){
                flashMode = FLASH_MODE_ON
            binding.flashBtn.setImageResource(R.drawable.ic_baseline_flash_on_24)
            initPreview()
            }
            else if (flashMode == FLASH_MODE_ON){
             flashMode = FLASH_MODE_AUTO
             binding.flashBtn.setImageResource(R.drawable.ic_baseline_flash_auto_24)
             initPreview()
         }
            else if (flashMode == FLASH_MODE_AUTO){
             flashMode = FLASH_MODE_OFF
             binding.flashBtn.setImageResource(R.drawable.ic_baseline_flash_off_24)
             initPreview()
         }
        }


    }



    fun initPreview(){
        cameraProcessFeature = ProcessCameraProvider.getInstance(this)

        cameraProcessFeature.addListener({
            var cameraProvider = cameraProcessFeature.get()
            var preview = Preview.Builder().build()


            preview.setSurfaceProvider(binding.preview.surfaceProvider)
            imageCapture = ImageCapture.Builder()
                .setFlashMode(flashMode)
                .build()

            try {

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(this , cameraSelector , imageCapture,  preview)

            }catch (e: Exception){

            }

        } ,
            ContextCompat.getMainExecutor(this)
        )

    }

    fun recordVideo(){
        val selector = QualitySelector.from(Quality.FHD , FallbackStrategy.higherQualityOrLowerThan(
            Quality.HD))
        val recorder = Recorder.Builder()
            .setQualitySelector(selector)
            .build()

    }



    fun imageCapture(){
        var imageCapture = imageCapture ?: return
//        val photoFile = File(
//            outputDirectory,
//            SimpleDateFormat(
//                "yy-MM-dd-HH-mm-ss-SSS",
//                Locale.getDefault()
//            ).format(System.currentTimeMillis()) + ".jpg"
//        )

        binding.preview.animate()
            .scaleY(0.7f)
            .scaleX(0.7f)
            .setDuration(100)
            .withEndAction {
                binding.preview.animate()
                    .scaleY(1f)
                    .scaleX(1f)
                    .setDuration(100)
                    .start()
            }.start()

//        var outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "${SimpleDateFormat( "yy-MM-dd-HH-mm-ss-SSS" , Locale.US).format(System.currentTimeMillis())}.jpg")
        }

        var outputFileOptions = ImageCapture.OutputFileOptions
            .Builder(this.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            .build()

        imageCapture.takePicture(outputFileOptions , ContextCompat.getMainExecutor(this) , object :ImageCapture.OnImageSavedCallback{
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                Toast.makeText(this@MainActivity, "succsessful", Toast.LENGTH_LONG).show()
            }

            override fun onError(exception: ImageCaptureException) {

            }

        } )
    }

}