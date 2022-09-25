package com.example.camera_square

import android.Manifest
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.*
import androidx.camera.video.internal.compat.Api23Impl.build
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.util.Consumer
import com.example.camera_square.databinding.ActivityMain2Binding
import com.google.common.util.concurrent.ListenableFuture
import java.lang.Exception
import java.util.*

class MainActivity2 : AppCompatActivity() {

    private lateinit var cameraProcessFeature: ListenableFuture<ProcessCameraProvider>
    lateinit var binding : com.example.camera_square.databinding.ActivityMain2Binding
    var cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

    var videoCapture : VideoCapture<Recorder>? = null
    var recording : Recording? = null
//    private var recorder: Recording? = null

    val selector = QualitySelector.from(Quality.HIGHEST)
    val recorder = Recorder.Builder()
        .setQualitySelector(selector)
        .build()

//    val recording = Recorder.Builder()
//        .setQualitySelector(selector)
//        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = com.example.camera_square.databinding.ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)


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

        binding.cardView.setOnClickListener{
            if (Build.VERSION.SDK_INT>=24)
            captureVideo()
        }



    }

    fun initPreview(){
        cameraProcessFeature = ProcessCameraProvider.getInstance(this)

        cameraProcessFeature.addListener({
            var cameraProvider = cameraProcessFeature.get()
            var preview = Preview.Builder().build()


            preview.setSurfaceProvider(binding.preview.surfaceProvider)




            videoCapture = VideoCapture.withOutput(recorder)



            try {

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(this , cameraSelector , videoCapture , preview)

            }catch (e: Exception){

            }

        } ,
            ContextCompat.getMainExecutor(this)
        )

    }


    // Implements VideoCapture use case, including start and stop capturing.
    @RequiresApi(Build.VERSION_CODES.N)
    private fun captureVideo() {
        val videoCapture = this.videoCapture ?: return

        binding.cardView.isEnabled = false

        val curRecording = recording
        if (curRecording != null) {
            // Stop the current recording session.
            curRecording.stop()
            recording = null
            return
        }

        // create and start a new recording session
        val name = SimpleDateFormat("yy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()
        recording = videoCapture.output
            .prepareRecording(this, mediaStoreOutputOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(this@MainActivity2,
                        Manifest.permission.RECORD_AUDIO) ==
                    PermissionChecker.PERMISSION_GRANTED)
                {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when(recordEvent) {
                    is VideoRecordEvent.Start -> {
                        binding.cardView.apply {
                            binding.text.text = "stop"
                            isEnabled = true
                        }
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg = "Video capture succeeded: " +
                                    "${recordEvent.outputResults.outputUri}"
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT)
                                .show()
//                            Log.d(TAG, msg)
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(TAG, "Video capture ends with error: " +
                                    "${recordEvent.error}")
                        }
                        binding.cardView.apply {
                            binding.text.text = "start"
                            isEnabled = true
                        }
                    }
                }
            }
    }






//    @RequiresApi(Build.VERSION_CODES.N)
//    fun videoCapture(){
//
//
//        val name = "CameraX-recording-" +
//                SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
//                    .format(System.currentTimeMillis()) + ".mp4"
//        val contentValues = ContentValues().apply {
//            put(MediaStore.Video.Media.DISPLAY_NAME, name)
//        }
//        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
//            this.contentResolver,
//            MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
//            .setContentValues(contentValues)
//            .build()
//
//        // configure Recorder and Start recording to the mediaStoreOutput.
//        currentRecording = videoCapture.output
//            .prepareRecording(this, mediaStoreOutput)
//            .start(ContextCompat.getMainExecutor(this),object :Consumer<VideoRecordEvent>{
//                override fun accept(t: VideoRecordEvent?) {
//
//                }
//
//            })
//
//
//
//
//    }
}