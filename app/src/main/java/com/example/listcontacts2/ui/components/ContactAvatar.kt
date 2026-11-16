package com.example.listcontacts2.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ContactAvatar(
    photoUri: String?,
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    textColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUri.isNullOrEmptyUri()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(Uri.parse(photoUri))
                    .crossfade(true)
                    .build(),
                contentDescription = "Photo du contact",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = null
            )
        } else if (initials.isNotBlank()) {
            Text(
                text = initials,
                color = textColor,
                fontSize = (size.value * 0.4).sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}

private fun String?.isNullOrEmptyUri(): Boolean {
    return this.isNullOrBlank()
}
