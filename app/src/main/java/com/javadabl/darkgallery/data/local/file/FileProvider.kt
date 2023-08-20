package com.javadabl.darkgallery.data.local.file

import android.content.ContentUris
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.javadabl.darkgallery.data.local.file.model.MediaFile
import com.javadabl.darkgallery.data.local.file.model.MediaType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.flow
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileProvider @Inject constructor(
    @ApplicationContext val context: Context
) {

    @RequiresApi(Build.VERSION_CODES.R)
    fun deleteUrisAPIAbove30(uris: List<Uri>) = flow<IntentSender> {
        emit(MediaStore.createDeleteRequest(context.contentResolver, uris).intentSender)
    }

    fun deleteUrisAPIBelow30(uris: List<Uri>): String {
        var successfulDeleteCount = 0

        uris.forEach {
            successfulDeleteCount += context.contentResolver.delete(it, null, null)
        }

        return "$successfulDeleteCount items deleted."
    }


    fun queryImages() = flow {
        val mediaFiles = findImageFiles() + findVideoFiles()
        emit(mediaFiles)
    }


    private fun findImageFiles(): ArrayList<MediaFile> {
        val imageFiles = ArrayList<MediaFile>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.WIDTH,
        )


        val sortOrder = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} DESC"




        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->


            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            val dateModifiedColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

            val directoryNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

            val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)

            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)


            while (cursor.moveToNext()) {

                val id = cursor.getLong(idColumn)
                val dateModified =
                    Date(TimeUnit.SECONDS.toMillis(cursor.getLong(dateModifiedColumn)))
                val displayName = cursor.getString(displayNameColumn)

                val directory = cursor.getString(directoryNameColumn) ?: ".root"

                val size = cursor.getLong(sizeColumn)

                val path = cursor.getString(dataColumn)

                val dateTaken = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(dateTakenColumn)))

                val resolution =
                    cursor.getString(widthColumn) + "x" + cursor.getString(heightColumn)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                imageFiles.add(
                    MediaFile(
                        contentUri,
                        displayName,
                        path,
                        directory,
                        dateModified,
                        dateTaken,
                        size,
                        resolution,
                        MediaType.Image
                    )
                )

            }
            cursor.close()
        }
        return imageFiles
    }


    private fun findVideoFiles(): ArrayList<MediaFile> {
        val videoFiles = ArrayList<MediaFile>()

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.RESOLUTION
        )


        val sortOrder = "${MediaStore.Video.Media.BUCKET_DISPLAY_NAME} DESC"


        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->


            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)

            val dateModifiedColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)

            val directoryNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

            val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)

            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

            val resolutionColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION)


            while (cursor.moveToNext()) {

                val id = cursor.getLong(idColumn)
                val dateModified =
                    Date(TimeUnit.SECONDS.toMillis(cursor.getLong(dateModifiedColumn)))
                val displayName = cursor.getString(displayNameColumn)

                val directory = cursor.getString(directoryNameColumn) ?: ".root"

                val size = cursor.getLong(sizeColumn)

                val path = cursor.getString(dataColumn)

                val resolution = cursor.getString(resolutionColumn)

                val dateTaken = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(dateTakenColumn)))

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                videoFiles.add(
                    MediaFile(
                        contentUri,
                        displayName,
                        path,
                        directory,
                        dateModified,
                        dateTaken,
                        size,
                        resolution,
                        MediaType.Video
                    )
                )

            }
            cursor.close()
        }
        return videoFiles
    }

}