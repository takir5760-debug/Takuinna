package com.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// =========================================================================================
// [১] অ্যাপ কনফিগারেশন ও সেটিংস
// =========================================================================================
object Config {
    const val DEFAULT_FACEBOOK_URL: String = "https://www.facebook.com/profile.php?id=61592872183574"
    const val FACEBOOK_USER_ID: String = "61592872183574"
    const val TERMINAL_MESSAGE: String = "Hello, how are you?"
    const val GOTCHA_MESSAGE: String = "GOTCHA, I'M COMING FOR U"
    const val CLOSE_APP_AFTER_OPEN: Boolean = false

    fun openFacebookLink(context: Context, customUrl: String? = null) {
        val targetUrl = customUrl?.takeIf { it.isNotBlank() } ?: DEFAULT_FACEBOOK_URL
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val chooser = Intent.createChooser(intent, "Open via Facebook or Browser")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(fallbackIntent)
            } catch (err: Exception) {
                Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// =========================================================================================
// [২] কালার ও থিম ডেফিনিশন
// =========================================================================================
val CyberBlack = Color(0xFF090C10)
val CyberDarkSurface = Color(0xFF131924)
val CyberGreen = Color(0xFF00FF66)
val CyberGreenDim = Color(0xFF00B344)
val CyberBlue = Color(0xFF00A2FF)
val CyberBlueGlass = Color(0x660077FF)
val CyberWhite = Color(0xFFF0F6FC)
val CyberMuted = Color(0xFF8B949E)
val FacebookBlue = Color(0xFF1877F2)
val SunOrange = Color(0xFFFF9800)

private val AppDarkColorScheme = darkColorScheme(
    primary = CyberBlue,
    secondary = CyberGreen,
    tertiary = CyberGreenDim,
    background = CyberBlack,
    surface = CyberDarkSurface,
    onPrimary = CyberWhite,
    onSecondary = CyberBlack,
    onBackground = CyberWhite,
    onSurface = CyberWhite
)

enum class ScreenState {
    BOOT_LOADING,
    PHOTO_1,
    PHOTO_2,
    HACKER_TERMINAL,
    OPEN_BUTTON,
    GOTCHA,
    CHMOD_SCAN
}

// =========================================================================================
// [৩] মেইন অ্যাক্টিভিটি
// =========================================================================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = AppDarkColorScheme) {
                TakuinnaApp()
            }
        }
    }
}

// =========================================================================================
// [৪] মূল অ্যাপ স্টেট ও ন্যাভিগেশন কন্ট্রোল
// =========================================================================================
@Composable
fun TakuinnaApp() {
    val context = LocalContext.current
    val sharedPrefs = remember {
        context.getSharedPreferences("takuinna_prefs", Context.MODE_PRIVATE)
    }

    var currentScreen by remember { mutableStateOf(ScreenState.BOOT_LOADING) }
    var facebookUrl by remember {
        mutableStateOf(sharedPrefs.getString("fb_url", Config.DEFAULT_FACEBOOK_URL) ?: Config.DEFAULT_FACEBOOK_URL
