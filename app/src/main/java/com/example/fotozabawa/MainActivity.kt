package com.example.fotozabawa

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.media.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.fotozabawa.Constants.TAG
import com.example.fotozabawa.api.RestApiService
import com.example.fotozabawa.databinding.ActivityMainBinding
import com.example.fotozabawa.model.database.SettingsDatabase
import com.example.fotozabawa.model.entity.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    private lateinit var arrow: ImageButton
    private lateinit var hiddenView: LinearLayout
    private lateinit var cardView: CardView

    private lateinit var binding: ActivityMainBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var settingsDatabase: SettingsDatabase

    var photoNumber: Int = 0
    var secondsNumber: Int = 0
    var secondsNumberEdit: Int = 0
    var themeNumber: Int = 0

    var orintationMode: Int = 0

    lateinit var sounds: Sounds

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        settingsDatabase = SettingsDatabase.getDatabase(this)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(binding.root)
        supportActionBar?.hide()

        val config = resources.configuration
        orintationMode = config.orientation

        // 1 - portrait
        // 2 - landscape

        val myCardView = findViewById<CardView>(R.id.base_cardview)

        if (orintationMode == 2) {
            myCardView.visibility = View.GONE
            binding.buttonTakePhotoPortrait.visibility = View.GONE
            binding.buttonTakePhotoLandscape.visibility = View.VISIBLE
        } else {
            myCardView.visibility = View.VISIBLE
            binding.buttonTakePhotoPortrait.visibility = View.VISIBLE
            binding.buttonTakePhotoLandscape.visibility = View.GONE
        }

        loadBanners()

        cardView = findViewById(R.id.base_cardview)
        arrow = findViewById(R.id.arrow_button)
        hiddenView = findViewById(R.id.hidden_view)

        val spinnerPhotoNumber = findViewById<Spinner>(R.id.photoNumberSpinner)
        val editTextSeconds = findViewById<EditText>(R.id.editSecondsNumber)
        val img1 = findViewById<ImageView>(R.id.imageView1)
        val img2 = findViewById<ImageView>(R.id.imageView2)
        val img3 = findViewById<ImageView>(R.id.imageView3)
        val img4 = findViewById<ImageView>(R.id.imageView4)
        val random = findViewById<ImageView>(R.id.imageViewRandom)

        img1.setOnClickListener {
            themeNumber = 1
            img1.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
            img2.setBackgroundColor(Color.parseColor("#AF3986AC"))
            img3.setBackgroundColor(Color.parseColor("#AF3986AC"))
            img4.setBackgroundColor(Color.parseColor("#AF3986AC"))
            random.setBackgroundColor(Color.parseColor("#AF3986AC"))
        }

        img2.setOnClickListener {
            themeNumber = 2
            img1.setBackgroundColor(Color.parseColor("#AF3986AC"))
            img2.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
            img3.setBackgroundColor(Color.parseColor("#AF3986AC"))
            img4.setBackgroundColor(Color.parseColor("#AF3986AC"))
            random.setBackgroundColor(Color.parseColor("#AF3986AC"))
        }

        img3.setOnClickListener {
            themeNumber = 3
            img1.setBackgroundColor(Color.parseColor("#AF3986AC"))
            img2.setBackgroundColor(Color.parseColor("#AF3986AC"))
            img3.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
            img4.setBackgroundColor(Color.parseColor("#AF3986AC"))
            random.setBackgroundColor(Color.parseColor("#AF3986AC"))
        }

        img4.setOnClickListener {
            themeNumber = 4
            img1.setBackgroundColor(Color.parseColor("#AF3986AC"))
            img2.setBackgroundColor(Color.parseColor("#AF3986AC"))
            img3.setBackgroundColor(Color.parseColor("#AF3986AC"))
            img4.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
            random.setBackgroundColor(Color.parseColor("#AF3986AC"))
        }

        random.setOnClickListener {
            themeNumber = 5
            img1.setBackgroundColor(Color.parseColor("#AF3986AC"))
            img2.setBackgroundColor(Color.parseColor("#AF3986AC"))
            img3.setBackgroundColor(Color.parseColor("#AF3986AC"))
            img4.setBackgroundColor(Color.parseColor("#AF3986AC"))
            random.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
        }

        arrow.setOnClickListener {
            // If the CardView is already expanded, set its visibility
            // to gone and change the expand less icon to expand more.
            if (hiddenView.visibility == View.VISIBLE) {
                // The transition of the hiddenView is carried out by the TransitionManager class.
                // Here we use an object of the AutoTransition Class to create a default transition
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    TransitionManager.beginDelayedTransition(cardView, AutoTransition())
                }
                hiddenView.visibility = View.GONE
                arrow.setImageResource(R.drawable.ic_baseline_expand_more_20)

                sendToDatabase(
                    editTextSeconds.text.toString().toInt(),
                    spinnerPhotoNumber.selectedItemPosition,
                    themeNumber
                )

            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    TransitionManager.beginDelayedTransition(cardView, AutoTransition())
                }
                hiddenView.visibility = View.VISIBLE
                arrow.setImageResource(R.drawable.ic_baseline_expand_less_20)
            }
        }

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                Constants.REQUIRED_PERMISSIONS,
                Constants.REQUEST_CODE_PERMISSIONS
            )
        }

        sounds = Sounds(this)

        binding.buttonTakePhotoPortrait.setOnClickListener {

            val photoNumberPosition = spinnerPhotoNumber.selectedItemPosition
            val pN = arrayOf(2, 3, 4, 8)
            photoNumber = pN[photoNumberPosition]
            secondsNumber = editTextSeconds.text.toString().toInt()
            secondsNumberEdit = secondsNumber * 1000

            takePhotosWithTimer()
        }

        binding.buttonTakePhotoLandscape.setOnClickListener {

            takePhotosWithTimer()
        }

        // Load from database
        GlobalScope.launch(Dispatchers.IO) {
            val sec = async { settingsDatabase.settingsDAO().getSeconds() }
            val pNumber = async { settingsDatabase.settingsDAO().getPhotoNumber() }
            val theme = async { settingsDatabase.settingsDAO().getTheme() }

            val editable = Editable.Factory.getInstance().newEditable(sec.await().toString())
            editTextSeconds.text = editable
            secondsNumber = sec.await().toString().toInt()
            secondsNumberEdit = secondsNumber * 1000
            spinnerPhotoNumber.setSelection(pNumber.await())
            val pN = arrayOf(2, 3, 4, 8)
            photoNumber = pN[pNumber.await()]

            setColorsFromDatabase(theme.await())
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(baseContext, "Landscape Mode", Toast.LENGTH_SHORT).show()
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(baseContext, "Portrait Mode", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun setColorsFromDatabase(number: Int) {
        val img1 = findViewById<ImageView>(R.id.imageView1)
        val img2 = findViewById<ImageView>(R.id.imageView2)
        val img3 = findViewById<ImageView>(R.id.imageView3)
        val img4 = findViewById<ImageView>(R.id.imageView4)
        val random = findViewById<ImageView>(R.id.imageViewRandom)

        if (number == 1) {
            img1.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
            img2.setBackgroundColor(Color.parseColor("#AF3986AC"))
            img3.setBackgroundColor(Color.parseColor("#AF3986AC"))
            img4.setBackgroundColor(Color.parseColor("#AF3986AC"))
            random.setBackgroundColor(Color.parseColor("#AF3986AC"))
        } else if (number == 2) {
            img1.setBackgroundColor(Color.parseColor("#AF3986AC"))
            img2.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
            img3.setBackgroundColor(Color.parseColor("#AF3986AC"))
            img4.setBackgroundColor(Color.parseColor("#AF3986AC"))
            random.setBackgroundColor(Color.parseColor("#AF3986AC"))
        } else if (number == 3) {
            img1.setBackgroundColor(Color.parseColor("#AF3986AC"))
            img2.setBackgroundColor(Color.parseColor("#AF3986AC"))
            img3.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
            img4.setBackgroundColor(Color.parseColor("#AF3986AC"))
            random.setBackgroundColor(Color.parseColor("#AF3986AC"))
        } else if (number == 4) {
            img1.setBackgroundColor(Color.parseColor("#AF3986AC"))
            img2.setBackgroundColor(Color.parseColor("#AF3986AC"))
            img3.setBackgroundColor(Color.parseColor("#AF3986AC"))
            img4.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
            random.setBackgroundColor(Color.parseColor("#AF3986AC"))
        } else if (number == 5) {
            img1.setBackgroundColor(Color.parseColor("#AF3986AC"))
            img2.setBackgroundColor(Color.parseColor("#AF3986AC"))
            img3.setBackgroundColor(Color.parseColor("#AF3986AC"))
            img4.setBackgroundColor(Color.parseColor("#AF3986AC"))
            random.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
        }
    }

    private fun sendToDatabase(secondsNumber: Int, photoNumber: Int, themeNumber: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            settingsDatabase.settingsDAO().deleteAll()
            val current = Settings(1, secondsNumber, photoNumber, themeNumber)
            settingsDatabase.settingsDAO().insert(current)
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let { mFile ->
            File(mFile, resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }

        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun takePhotosWithTimer() {
        Timer().scheduleAtFixedRate(object : TimerTask() {
            private var timeCount: Int = secondsNumber
            private var photoCount: Int = 1
            override fun run() {
                if(timeCount == 0 && photoCount == photoNumber) {
                    Log.i("timer", "timeCount == 0 && photoCount == photoNumber")
                    sounds.shutter()
                    takePhoto()
                }
                else if(timeCount == 0) {
                    Log.i("timer", "timeCount == 0")
                    sounds.shutter()
                    takePhoto()
                    photoCount += 1
                    timeCount = secondsNumber + 1
                }
                else if(timeCount == -1) {
                    sounds.done()
                    makePdf()
                    this.cancel()
                }
                else if(timeCount <= 3){
                    Log.i("timer", "timeCount <= 3")
                    sounds.bip()
                }
                timeCount -= 1
            }
        }, 0, 1000)
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                Constants.FILE_NAME_FORMAT,
                Locale.getDefault()
            )
                .format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOption = ImageCapture
            .OutputFileOptions
            .Builder(photoFile)
            .build()

        imageCapture.takePicture(
            outputOption,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo saved"

//                    Toast.makeText(
//                        this@MainActivity,
//                        "$msg $savedUri",
//                        Toast.LENGTH_LONG
//                    ).show()
                    uploadPhoto(photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(
                        TAG, "onError: ${exception.message}",
                        exception
                    )
                }

            }
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider
            .getInstance(this)
        cameraProviderFuture.addListener({

            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { mPreview ->
                    mPreview.setSurfaceProvider(
                        binding.viewFinder.surfaceProvider
                    )
                }
            imageCapture = ImageCapture.Builder()
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector,
                    preview, imageCapture
                )
            } catch (e: Exception) {
                Log.d(TAG, "startCamera Fail:", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_CODE_PERMISSIONS) {
            if (allPermissionGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permission not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionGranted() =
        Constants.REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                baseContext, it
            ) == PackageManager.PERMISSION_GRANTED
        }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun loadBanners() {

        val img1 = findViewById<ImageView>(R.id.imageView1)
        img1.setImageResource(R.drawable.mountain)

        val img2 = findViewById<ImageView>(R.id.imageView2)
        img2.setImageResource(R.drawable.forest)

        val img3 = findViewById<ImageView>(R.id.imageView3)
        img3.setImageResource(R.drawable.sunset)

        val img4 = findViewById<ImageView>(R.id.imageView4)
        img4.setImageResource(R.drawable.sea)

    }

    private fun uploadPhoto(file: File) {
        val apiService = RestApiService()
        apiService.uploadPhoto(file, this.orintationMode == 1, this)
    }

    private fun makePdf() {
        val apiService = RestApiService()
        apiService.printPdf(themeNumber, this, supportFragmentManager)
    }
}
