package com.darach.calendarwidget.widget.ui

import android.content.Context
import android.graphics.Bitmap
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.transformations
import coil3.toBitmap
import coil3.transform.CircleCropTransformation
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Decodes attendee photo thumbnails (content:// URIs) into small circular bitmaps. */
@Singleton
class AvatarLoader
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) {
        private val imageLoader by lazy { ImageLoader.Builder(context).build() }

        suspend fun load(uris: Collection<String>): Map<String, Bitmap> =
            uris
                .distinct()
                .take(MAX_AVATARS)
                .mapNotNull { uri ->
                    val result =
                        imageLoader.execute(
                            ImageRequest
                                .Builder(context)
                                .data(uri)
                                .size(SIZE_PX)
                                .transformations(CircleCropTransformation())
                                .build(),
                        )
                    (result as? SuccessResult)?.image?.toBitmap()?.let { uri to it }
                }.toMap()

        private companion object {
            const val MAX_AVATARS = 12
            const val SIZE_PX = 96
        }
    }
