package com.example.k2024_04_21_livedata_volley

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.k2024_04_21_livedata_volley.databinding.ActivityMainBinding
import com.example.k2024_04_21_livedata_volley.models.JSON_MetMuseum
import com.example.k2024_04_21_livedata_volley.view_models.UrlViewModel
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val gson = Gson()
    private val metPublicDomainUrl = "https://collectionapi.metmuseum.org/public/collection/v1/objects/"
    private var imageData: JSON_MetMuseum? = null
    private lateinit var volleyQueue: RequestQueue

    private lateinit var yesButton: Button
    private lateinit var noButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        volleyQueue = Volley.newRequestQueue(this)
        val uriViewModel: UrlViewModel by viewModels()

        binding.loadImageMetaDataButton.setOnClickListener {
            loadImageMetadata(uriViewModel)
            resetButtons()
        }

        binding.nextImageButton.setOnClickListener {
            loadNextImage(uriViewModel)
        }

        // Create "Do you like this image?" prompt
        createLikePrompt()
    }

    private fun createLikePrompt() {
        val likePrompt = LinearLayout(this)
        likePrompt.orientation = LinearLayout.HORIZONTAL
        likePrompt.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, // Match parent width
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        likePrompt.gravity = android.view.Gravity.CENTER_HORIZONTAL // Center horizontally

        val promptText = TextView(this)
        promptText.text = "Do you like this image?"
        promptText.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        promptText.textSize = 23f // text size 23sp

        val buttonLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        buttonLayoutParams.gravity = android.view.Gravity.CENTER // Center buttons horizontally

        yesButton = Button(this)
        yesButton.text = "Yes"
        yesButton.layoutParams = buttonLayoutParams
        yesButton.textSize = 16f // text size of Yes button 16sp
        yesButton.setOnClickListener {
            Log.d("MainActivity", "User liked the image")
            // Handle Yes button click
            // Set background color to green
            yesButton.setBackgroundColor(Color.GREEN)
            // Reset background color of No button
            noButton.setBackgroundColor(Color.TRANSPARENT)
        }

        noButton = Button(this)
        noButton.text = "No"
        noButton.layoutParams = buttonLayoutParams
        noButton.textSize = 16f // Set text size of No button to 16sp
        noButton.setOnClickListener {
            Log.d("MainActivity", "User didn't like the image")
            // Handle No button click
            // Set background color to red
            noButton.setBackgroundColor(Color.RED)
            // Reset background color of Yes button
            yesButton.setBackgroundColor(Color.TRANSPARENT)
        }

        likePrompt.addView(promptText)
        likePrompt.addView(yesButton)
        likePrompt.addView(noButton)

        binding.root.addView(likePrompt)
    }

    private fun loadImageMetadata(uriViewModel: UrlViewModel) {
        // Reset buttons
        resetButtons()

        val nextIndex = uriViewModel.nextImageNumber()
        val metUrl = metPublicDomainUrl + nextIndex.toString()
        uriViewModel.setMetaDataUrl(metUrl)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, uriViewModel.getMetaDataUrl(), null,
            { response ->
                imageData = gson.fromJson(response.toString(), JSON_MetMuseum::class.java)
                uriViewModel.setImageUrl(imageData?.primaryImage ?: "Foobar")
                updateUIWithMetadata(imageData)
            },
            { error -> Log.i("SM", "Error: ${error}") }
        )
        volleyQueue.add(jsonObjectRequest)
    }

    private fun updateUIWithMetadata(metadata: JSON_MetMuseum?) {
        binding.tvImageTitle.text = metadata?.title ?: "Unknown Title"
        binding.tvImageDescription.text = metadata?.artistDisplayName ?: "Unknown Artist"
    }

    private fun loadNextImage(uriViewModel: UrlViewModel) {
        val imageUrl = uriViewModel.getImageUrl()
        binding.imageView.load(imageUrl) {
            crossfade(true)
            placeholder(R.drawable.loading_animation)
        }
    }

    private fun resetButtons() {
        yesButton.setBackgroundColor(Color.TRANSPARENT)
        noButton.setBackgroundColor(Color.TRANSPARENT)
    }
}
