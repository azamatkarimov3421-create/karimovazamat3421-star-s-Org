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
    size: Dp = 48.dp
) {
    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                try {
                    val pm = context.packageManager
                    val iconDrawable: Drawable = pm.getApplicationIcon(packageName)
                    setImageDrawable(iconDrawable)
                } catch (e: Exception) {
                    try {
                        val iconDrawable: Drawable = context.packageManager.getDefaultActivityIcon()
                        setImageDrawable(iconDrawable)
                    } catch (ex: Exception) {
                        // Keep blank or handle
                    }
                }
            }
        },
        update = { imageView ->
            try {
                val pm = imageView.context.packageManager
                val iconDrawable: Drawable = pm.getApplicationIcon(packageName)
                imageView.setImageDrawable(iconDrawable)
            } catch (e: Exception) {
                try {
                    val iconDrawable: Drawable = imageView.context.packageManager.getDefaultActivityIcon()
                    imageView.setImageDrawable(iconDrawable)
                } catch (ex: Exception) {
                    // Do nothing
                }
            }
        },
        modifier = modifier.size(size)
    )
}
