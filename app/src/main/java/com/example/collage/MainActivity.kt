package com.example.collage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "MAIN_ACTIVITY"

class MainActivity : AppCompatActivity() {

//    private lateinit var imageButton1: ImageButton

    private lateinit var imageButtons: List<ImageButton>

    private lateinit var mainView: View

//    private var newPhotoPath: String? = null
//    private var visibleImagePath: String? = null

    private var photoPaths: ArrayList<String?> = arrayListOf(null, null, null, null)

    private var whichImageIndex: Int? = null

    private var currentPhotoPath: String? = null

//    private val NEW_PHOTO_PATH_KEY = "new photo path key"
//    private val VISIBLE_IMAGE_PATH_KEY = "visible image path key"

    private val PHOTO_PATH_LIST_ARRAY_KEY = "photo path list key"
    private val IMAGE_INDEX_KEY = "image index key"
    private val CURRENT_PHOTO_PATH_KEY = "current photo path key"

    private val cameraActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result -> handleIamge(result)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        newPhotoPath = savedInstanceState?.getString(NEW_PHOTO_PATH_KEY)
//        visibleImagePath = savedInstanceState?.getString(VISIBLE_IMAGE_PATH_KEY)

        whichImageIndex = savedInstanceState?.getInt(IMAGE_INDEX_KEY)
        currentPhotoPath = savedInstanceState?.getString(CURRENT_PHOTO_PATH_KEY)
        photoPaths = savedInstanceState?.getStringArrayList(PHOTO_PATH_LIST_ARRAY_KEY) ?: arrayListOf(null, null, null, null)

        mainView = findViewById(R.id.content)

//        imageButton1 = findViewById(R.id.imageButton1)
//        imageButton1.setOnClickListener {
//            takePicture()
//        }
        imageButtons = listOf<ImageButton>(
            findViewById(R.id.imageButton1),
            findViewById(R.id.imageButton2),
            findViewById(R.id.imageButton3),
            findViewById(R.id.imageButton4)
        )

        for (imageButton in imageButtons) {
            imageButton.setOnClickListener { imgbtn ->
                takePictureFor(imgbtn as ImageButton)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(PHOTO_PATH_LIST_ARRAY_KEY, photoPaths)
        outState.putString(CURRENT_PHOTO_PATH_KEY, currentPhotoPath)
        whichImageIndex?.let { index -> outState.putInt(IMAGE_INDEX_KEY, index) }
    }

    private fun takePictureFor(imageButton: ImageButton) {

        val index = imageButtons.indexOf(imageButton)
        whichImageIndex = index

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val (photoFile, photoFilePath) = createIamgeFile()
        if (photoFile != null) {
            currentPhotoPath = photoFilePath
            val photoUri = FileProvider.getUriForFile(this, "com.example.collage.fileprovider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            cameraActivityLauncher.launch(takePictureIntent)
        }

    }

    private fun createIamgeFile(): Pair<File?, String?> {
        try {
            val dateTime = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val imageFileName = "COLLAGE_${dateTime}"
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File.createTempFile(imageFileName, ".jpg", storageDir)
            val filePath = file.absolutePath
            return file to filePath
        } catch (ex: IOException) {
            return null to null
        }
    }
    
    private fun handleIamge(result: ActivityResult) {
        when (result.resultCode) {
            RESULT_OK -> {
                Log.d(TAG, "result ok, user took picture, image at $currentPhotoPath")
                whichImageIndex?.let { index -> photoPaths[index] = currentPhotoPath}
            }
            RESULT_CANCELED -> {
                Log.d(TAG, "Result canceled, no picture taken")
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Log.d(TAG, "on window focus changed $hasFocus visible image at $currentPhotoPath")
        if (hasFocus) {
//            visibleImagePath?.let { imagePath ->
//                loadImage(imageButton1, imagePath)
            imageButtons.zip(photoPaths) { imageButton, photoPath ->
                photoPath?.let {
                    loadImage(imageButton, photoPath)
                }
            }

//            for (index in 0 until imageButtons.size) {
//                val photoPath = photoPaths[index]
//                val imageButton = imageButtons[index]
//                if (photoPath != null) {
//                    loadImage(imageButton, photoPath)
//                }
//            }

        }
    }


    private fun loadImage(imageButton: ImageButton, imagePath: String) {
        Picasso.get().load(File(imagePath)).error(android.R.drawable.stat_notify_error).fit().centerCrop().into(imageButton, object: Callback {
            override fun onSuccess() {
                Log.d(TAG, "Loaded image $imagePath")
            }

            override fun onError(e: Exception?) {
                Log.e(TAG, "Error loading image $imagePath", e)
            }
        })
    }
}