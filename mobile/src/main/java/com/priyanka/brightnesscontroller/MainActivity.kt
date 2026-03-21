package com.priyanka.brightnesscontroller

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import com.priyanka.brightnesscontroller.ui.theme.BrightnessControllerTheme
import com.priyanka.brightnesscontroller.ui.theme.TealPrimary
import com.priyanka.brightnesscontroller.ui.theme.TealPrimaryDark
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private val brightnessController: BrightnessController by inject()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (!brightnessController.hasWriteSettingsPermission()) {
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Request permissions on startup if not granted (Required for API 23+)
        if (!brightnessController.hasWriteSettingsPermission()) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, "package:$packageName".toUri())
            permissionLauncher.launch(intent)
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

data class Language(val name: String, val code: String)

val supportedLanguages = listOf(
    Language("Default (System)", ""),
    Language("English", "en"),
    Language("Español (Spanish)", "es"),
    Language("Français (French)", "fr"),
    Language("Indonesian", "id"),
    Language("اردو (Urdu)", "ur"),
    Language("عربي (Arabic)", "ar"),
    Language("ไทย (Thai)", "th"),
    Language("हिन्दी (Hindi)", "hi"),
    Language("ଓଡ଼ିଆ (Odia)", "or"),
    Language("తెలుగు (Telugu)", "te"),
    Language("ಕನ್ನಡ (Kannada)", "kn"),
    Language("मराठी (Marathi)", "mr"),
    Language("ತುಳು (Tulu)", "tcy"),
    Language("বাংলা (Bengali)", "bn")
)

@Composable
fun LanguageSelectionDropdown() {
    var expanded by remember { mutableStateOf(false) }
    val currentLocale = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    val selectedLanguage = supportedLanguages.find { it.code == currentLocale } ?: supportedLanguages[0]

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.select_language),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.height(56.dp) // Taller button for easier tapping
            ) {
                Text(
                    text = "🌐 ${selectedLanguage.name}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                supportedLanguages.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(language.name) },
                        onClick = {
                            val appLocale: LocaleListCompat = if (language.code.isEmpty()) {
                                LocaleListCompat.getEmptyLocaleList()
                            } else {
                                LocaleListCompat.forLanguageTags(language.code)
                            }
                            AppCompatDelegate.setApplicationLocales(appLocale)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BrightnessScreen(brightnessController: BrightnessController) {
    val context = LocalContext.current
    var brightness by remember { mutableFloatStateOf(brightnessController.getSystemBrightness().toFloat()) }
    
    val shareSubject = stringResource(R.string.share_subject)
    val shareTextTemplate = stringResource(R.string.share_text)
    val noShareApp = stringResource(R.string.no_share_app)
    val emailSubject = stringResource(R.string.email_subject)
    val noEmailApp = stringResource(R.string.no_email_app)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LanguageSelectionDropdown()

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        Text(text = stringResource(R.string.brightness_level, brightness.toInt()))

        Slider(
            value = brightness,
            onValueChange = { newValue ->
                brightness = newValue
                if (brightnessController.hasWriteSettingsPermission()) {
                    brightnessController.setSystemBrightness(newValue.toInt())
                }

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            // FORCE COLORS TO RESET THE THUMB SHAPE
            colors = SliderDefaults.colors(
                thumbColor = TealPrimaryDark,           // This forces the thumb to be your Teal
                activeTrackColor = TealPrimary,      // The line to the left
                inactiveTrackColor = TealPrimary.copy(alpha = 0.30f), // The line to the right
                activeTickColor = Color.Transparent, // REMOVES THE DOTS
                inactiveTickColor = Color.Transparent // REMOVES THE DOTS
            )
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
            Text(text = stringResource(R.string.rate_app))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            val shareBody = "https://play.google.com/store/apps/details?id=${context.packageName}"
            val sharingIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, shareSubject)
                putExtra(Intent.EXTRA_TEXT, shareTextTemplate.format(shareBody))
            }
            try {
                context.startActivity(Intent.createChooser(sharingIntent, "Share via"))
            } catch (_: Exception) {
                Toast.makeText(context, noShareApp, Toast.LENGTH_SHORT).show()
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.share_app))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:".toUri()
                putExtra(Intent.EXTRA_EMAIL, arrayOf("appfeedbackpriyanka@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, emailSubject)
            }
            try {
                context.startActivity(Intent.createChooser(emailIntent, "Send mail"))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, noEmailApp, Toast.LENGTH_SHORT).show()
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.feedback))
        }
    }
}
