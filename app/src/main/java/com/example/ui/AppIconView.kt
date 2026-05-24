package com.example.ui

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun AppIconView(
    packageName: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconTintName: String = "none"
) {
    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                try {
                    if (packageName == "com.novax.settings") {
                        val drawable = context.resources.getDrawable(android.R.drawable.ic_menu_preferences, context.theme)
                        setImageDrawable(drawable)
                    } else {
                        val pm = context.packageManager
                        val iconDrawable: Drawable = pm.getApplicationIcon(packageName)
                        setImageDrawable(iconDrawable)
                    }
                } catch (e: Exception) {
                    try {
                        val iconDrawable: Drawable = context.packageManager.getDefaultActivityIcon()
                        setImageDrawable(iconDrawable)
                    } catch (ex: Exception) {
                        // Keep blank or handle
                    }
                }
                
                // Set initial tint
                val tint = getTintFilterColor(iconTintName)
                if (tint != null) {
                    setColorFilter(tint, android.graphics.PorterDuff.Mode.SRC_ATOP)
                } else {
                    clearColorFilter()
                }
            }
        },
        update = { imageView ->
            try {
                if (packageName == "com.novax.settings") {
                    val drawable = imageView.context.resources.getDrawable(android.R.drawable.ic_menu_preferences, imageView.context.theme)
                    imageView.setImageDrawable(drawable)
                } else {
                    val pm = imageView.context.packageManager
                    val iconDrawable: Drawable = pm.getApplicationIcon(packageName)
                    imageView.setImageDrawable(iconDrawable)
                }
            } catch (e: Exception) {
                try {
                    val iconDrawable: Drawable = imageView.context.packageManager.getDefaultActivityIcon()
                    imageView.setImageDrawable(iconDrawable)
                } catch (ex: Exception) {
                    // Do nothing
                }
            }
            
            // Apply dynamic tint update
            val tint = getTintFilterColor(iconTintName)
            if (tint != null) {
                imageView.setColorFilter(tint, android.graphics.PorterDuff.Mode.SRC_ATOP)
            } else {
                imageView.clearColorFilter()
            }
        },
        modifier = modifier.size(size)
    )
}

private fun getTintFilterColor(tintName: String): Int? {
    return when (tintName) {
        "golden_glow" -> android.graphics.Color.argb(100, 255, 215, 0) // Amber gold tint
        "cyan_neon" -> android.graphics.Color.argb(100, 0, 255, 255) // Cyber cyan
        "emerald_green" -> android.graphics.Color.argb(100, 16, 185, 129) // Emerald green
        "rose_petal" -> android.graphics.Color.argb(100, 244, 114, 182) // Romantic pink
        "cyber_punk_pink" -> android.graphics.Color.argb(100, 255, 0, 128) // Deep magenta
        else -> null
    }
}
