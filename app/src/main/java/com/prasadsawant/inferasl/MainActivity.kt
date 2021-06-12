package com.prasadsawant.inferasl

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.prasadsawant.inferasl.databinding.ActivityMainBinding
import com.prasadsawant.inferasl.tflite.Classifier
import org.koin.android.ext.android.inject
import java.math.RoundingMode
import kotlin.math.pow

const val REQUEST_IMAGE_CAPTURE = 0


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private lateinit var binding: ActivityMainBinding

    private val classifier: Classifier by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (!hasPermission()) {
            requestPermission()
        }

        binding.btnTakePicture.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } catch (e: ActivityNotFoundException) {
                Log.e(TAG, e.localizedMessage)
            }
        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            if (imageBitmap != null) {
                binding.ivData.visibility = View.VISIBLE
                binding.ivData.setImageBitmap(imageBitmap)

                binding.tvInference.visibility = View.VISIBLE
                binding.tvConfidence.visibility = View.VISIBLE
                binding.tvInferenceTime.visibility = View.VISIBLE

                val begin = System.nanoTime()
                val result = classifier.recognizeImage(imageBitmap)
                val end = System.nanoTime()
                val diff = ((end - begin) * (10.0).pow(6)).toBigDecimal().setScale(1, RoundingMode.UP).toDouble()

                binding.tvInference.text = "Inference Result: ${result[0].title}"
                binding.tvConfidence.text = "Confidence: ${result[0].confidence}"
                binding.tvInferenceTime.text = "Inference Time: ${diff}ms"
            }
        }
    }


    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(PERMISSION_READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(PERMISSION_STORAGE) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(PERMISSION_READ_EXTERNAL_STORAGE)) {
                Toast.makeText(
                    this@MainActivity,
                    "Following permissions are required for this demo",
                    Toast.LENGTH_LONG)
                    .show()
            }
            requestPermissions(arrayOf(PERMISSION_READ_EXTERNAL_STORAGE, PERMISSION_STORAGE), PERMISSIONS_REQUEST)
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST = 1

        private const val PERMISSION_READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE

        private const val PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    }
}