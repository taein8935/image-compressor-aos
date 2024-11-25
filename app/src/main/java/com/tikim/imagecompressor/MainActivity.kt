package com.tikim.imagecompressor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tikim.imagecompressor.ui.theme.ImageCompressorTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imageCompressor = ImageCompressor(context = this)

        enableEdgeToEdge()
        setContent {
            ImageCompressorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        PhotoPickerScreen(imageCompressor)
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoPickerScreen(
    imageCompressor: ImageCompressor,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var originalBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var compressedBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }


    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { contentUri ->
        if (contentUri == null) return@rememberLauncherForActivityResult

        scope.launch {
            launch {
                originalBitmap = context.contentResolver.openInputStream(contentUri).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            }
            launch {
                compressedBitmap = imageCompressor.compressImage(contentUri, 1 * 1024L)
            }
        }
    }



    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center

    ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(
                                mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                ) {
                    Text(
                        text = "사진 선택"
                    )
                }

                if (originalBitmap != null) {
                    Image(
                        bitmap = originalBitmap!!.asImageBitmap(),
                        contentDescription = null,
                    )
                }

                if (compressedBitmap != null) {
                    Image(
                        bitmap = compressedBitmap!!.asImageBitmap(),
                        contentDescription = null,
                    )
                }

            }
    }
}