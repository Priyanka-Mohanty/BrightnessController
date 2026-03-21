package com.priyanka.brightnesscontroller

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.priyanka.brightnesscontroller.ui.theme.BrightnessControllerTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val brightnessController: BrightnessController by inject()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (!brightnessController.hasWriteSettingsPermission()) {
            Toast.makeText(this, "Permission to write settings denied. App requires this.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Request permissions on startup if not granted (Required for API 23+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!brightnessController.hasWriteSettingsPermission()) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, "package:$packageName".toUri())
                permissionLauncher.launch(intent)
            }
        }

        setContent {
            BrightnessControllerTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BrightnessScreen(brightnessController)
                }
            }
        }
    }
}

@Composable
fun BrightnessScreen(brightnessController: BrightnessController) {
    val context = LocalContext.current
    var brightness by remember { mutableFloatStateOf(brightnessController.getSystemBrightness().toFloat()) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Brightness Controller",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Brightness Level: ${brightness.toInt()}")
        
        Slider(
            value = brightness,
            onValueChange = { newValue ->
                brightness = newValue
                if (brightnessController.hasWriteSettingsPermission()) {
                    brightnessController.setSystemBrightness(newValue.toInt())
                }
                
                // Note: The legacy app also updated layoutpars.screenBrightness
                var activity = context as? ComponentActivity
                var ctx = context
                while (activity == null && ctx is android.content.ContextWrapper) {
                    ctx = ctx.baseContext
                    activity = ctx as? ComponentActivity
                }
                
                if (activity?.window != null) {
                    val attrs = activity.window.attributes
                    attrs.screenBrightness = newValue / 255f
                    activity.window.attributes = attrs
                }
            },
            valueRange = 0f..255f,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(onClick = {
            val uri = "market://details?id=${context.packageName}".toUri()
            val goToMarket = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            }
            try {
                context.startActivity(goToMarket)
            } catch (_: ActivityNotFoundException) {
                context.startActivity(Intent(Intent.ACTION_VIEW, "http://play.google.com/store/apps/details?id=${context.packageName}".toUri()))
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Rate App")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            val shareBody = "https://play.google.com/store/apps/details?id=${context.packageName}"
            val sharingIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Brightness Controller Application")
                putExtra(Intent.EXTRA_TEXT, "Brightness Controller:\n\n(Open it in Google Play Store to Download the Application)\n$shareBody")
            }
            try {
                context.startActivity(Intent.createChooser(sharingIntent, "Share via"))
            } catch (_: Exception) {
                Toast.makeText(context, "No app installed for sharing.", Toast.LENGTH_SHORT).show()
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Share App")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:".toUri()
                putExtra(Intent.EXTRA_EMAIL, arrayOf("appfeedbackpriyanka@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "User Feedback : Brightness Controller Application")
            }
            try {
                context.startActivity(Intent.createChooser(emailIntent, "Send mail"))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, "No email client installed.", Toast.LENGTH_SHORT).show()
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Feedback")
        }
    }
}
