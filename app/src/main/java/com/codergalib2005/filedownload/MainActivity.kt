package com.codergalib2005.filedownload

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.codergalib2005.filedownload.ui.theme.FileDownloadTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, you can proceed with downloading
            } else {
                // Permission denied, handle accordingly
                // You may want to show a message or disable the download functionality
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FileDownloadTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Call your Composable function here
                    FileDownloadScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FileDownloadScreen() {
    // Create a scaffold state to control the app bar

    // State to handle downloading state
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0) }

    // Get the context
    val context = LocalContext.current

    // Get the URI handler
    val uriHandler = LocalUriHandler.current

    // Get the keyboard controller
    val keyboardController = LocalSoftwareKeyboardController.current

    // Your existing code...

     Log.d("ApplicationLog", "is downloading - $isDownloading")

            // Content of your screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Download button
                Button(
                    onClick = {
                            if (hasStoragePermission(context)) {
                                keyboardController?.hide()
                                isDownloading = true
                                CoroutineScope(Dispatchers.IO).launch {
                                    downloadAudio(context) { progress ->
                                        // Update downloadProgress on the main thread
                                        downloadProgress = progress as Int
                                    }
                                    // Update isDownloading and downloadProgress on the main thread
                                    isDownloading = false
                                    downloadProgress = 0
                                }
                            } else {
                                requestStoragePermission(context = context)
                            }
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    enabled = !isDownloading, // Disable the button when isDownloading is true
                    colors = ButtonDefaults.buttonColors(if (!isDownloading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                ) {
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Download Audio")
                }

                // Display the progress indicator when downloading
                if (isDownloading) {
                    Column {
                        Text(
                            text = "$downloadProgress / 100",
                            modifier = Modifier
                                .fillMaxWidth(),
                            textAlign = TextAlign.Right
                        )
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            progress = downloadProgress / 100f // LinearProgressIndicator expects a float between 0 and 1
                        )
                    }
                }


                // Your existing content...
            }
        }

/**
 * Check if the app has storage permission.
 */
private fun hasStoragePermission(context: android.content.Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
}

/**
 * Request storage permission.
 */
private fun requestStoragePermission(context: Context) {
    ActivityCompat.requestPermissions(
        context as Activity,
        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
        1
    )
}

private suspend fun downloadAudio(context: Context, onProgressUpdate: (Int) -> Unit) {
    val url = "https://res.cloudinary.com/dpolvpc0n/video/upload/v1704967152/Quran%20App/024_yv5ayg.mp3"

    val cacheDirectory = context.cacheDir
    val fileName = "audio.mp3"
    val outputFile = File(cacheDirectory, fileName)

    try {
        val urlConnection = URL(url).openConnection() as HttpURLConnection
        val inputStream = urlConnection.inputStream
        val outputStream = FileOutputStream(outputFile)

        val totalFileSize = urlConnection.contentLength // Get the total file size
        var downloadedSize = 0 // Initialize downloaded size

        val buffer = ByteArray(16 * 1024) // Increase buffer size for more accurate progress

        var bytesRead: Int

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
            downloadedSize += bytesRead

            // Calculate and update download progress
            val progress = ((downloadedSize.toDouble() / totalFileSize) * 100).toInt()
            onProgressUpdate(progress)
        }

        inputStream.close()
        outputStream.close()

    } catch (e: Exception) {
        e.printStackTrace()
        // Handle the download failure
    }
}
