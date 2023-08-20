package com.javadabl.darkgallery.ui.externalbrowser

import android.app.Application
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.javadabl.darkgallery.DarkGalleryApp
import com.javadabl.darkgallery.data.local.file.model.MediaType
import com.javadabl.darkgallery.domain.model.Album
import com.javadabl.darkgallery.domain.model.Media
import com.javadabl.darkgallery.util.C
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import javax.inject.Inject

private const val TAG = "MyTag"

@HiltViewModel
class ExternalBrowserViewModel @Inject constructor(application: Application) :
    AndroidViewModel(application) {


    var currentViewingPosition = -1
    private var mAlbum = MutableLiveData<Album>()
    val album get() = mAlbum


    fun getAlbumFromUri(uri: Uri, type: MediaType) {

        viewModelScope.launch {
            withContext(IO) {
                val imageCursor = getImageCursor(uri)
                val videoCursor = getVideoCursor(uri)
                var finalMediaList = ArrayList(getImages(imageCursor) + getVideos(videoCursor))
                if (finalMediaList.size == 0)
                    when (type) {
                        MediaType.Image -> {
                            finalMediaList = imageLastStand(uri)
                        }
                        MediaType.Video -> {
                            finalMediaList = videoLastStand(uri)
                        }
                    }
                if (finalMediaList.size > 0) {
                    val album = Album(finalMediaList, finalMediaList[0].albumName)
                    album.media.sortBy {
                        it.name
                    }
                    mAlbum.postValue(album)
                }
            }

        }

    }


    private fun getImageCursor(uri: Uri): Cursor? {
        val cursor: Cursor?

        val resolver = getApplication<DarkGalleryApp>().contentResolver

        val file = File(uri.path!!)
        val parentFile = file.parentFile

        val projection = arrayOf(
            C.ID_IMAGE,
            C.NAME_IMAGE,
            C.DATEM_IMAGE,
            C.ALBUM_IMAGE,
            C.SIZE_IMAGE,
            C.DATET_IMAGE,
            C.DATA_IMAGE,
            C.HEIGHT_IMAGE,
            C.WIDTH_IMAGE,
        )

        var decodedParentPath = ""
        decodedParentPath = try {

            parentFile!!.path.substring(
                parentFile.path.indexOf(
                    "/storage",
                    ignoreCase = true
                )
            )
        } catch (e: Exception) {
            parentFile?.path ?: ""
        }


        var selection =
            "${C.DATA_IMAGE} like? " +
                    "and ${C.ALBUM_IMAGE} =?"


        var selectionArgs = arrayOf(
            "%$decodedParentPath%",
            file.parentFile!!.name
        )

        if (decodedParentPath == Environment.getExternalStorageDirectory().path) {
            selection =
                "${C.DATA_IMAGE} like? "

            selectionArgs = arrayOf(
                file.path.substring(file.path.indexOf("/storage"))
            )
        }



        cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )


        return cursor
    }


    private fun getVideoCursor(uri: Uri): Cursor? {
        val cursor: Cursor?

        val resolver = getApplication<DarkGalleryApp>().contentResolver

        val file = File(uri.path!!)
        val parentFile = file.parentFile

        val projection = arrayOf(
            C.ID_VIDEO,
            C.NAME_VIDEO,
            C.DATEM_VIDEO,
            C.ALBUM_VIDEO,
            C.SIZE_VIDEO,
            C.DATET_VIDEO,
            C.DATA_VIDEO,
            C.RES_VIDEO
        )

        var decodedParentPath = ""
        decodedParentPath = try {

            parentFile!!.path.substring(
                parentFile.path.indexOf(
                    "/storage",
                    ignoreCase = true
                )
            )
        } catch (e: Exception) {
            parentFile?.path ?: ""
        }


        var selection =
            "${C.DATA_VIDEO} like? " +
                    "and ${C.ALBUM_VIDEO} =?"


        var selectionArgs = arrayOf(
            "%$decodedParentPath%",
            file.parentFile!!.name
        )

        if (decodedParentPath == Environment.getExternalStorageDirectory().path) {
            selection =
                "${C.DATA_VIDEO} like? "

            selectionArgs = arrayOf(
                file.path.substring(file.path.indexOf("/storage"))
            )
        }


        cursor = resolver.query(
            C.EXTERNAL_VIDEO,
            projection,
            selection,
            selectionArgs,
            null
        )

        return cursor
    }


    private fun getImages(cursor: Cursor?): ArrayList<Media> {
        val mediaList = ArrayList<Media>()
        try {
            if (cursor != null) {
                val IDC = cursor.getColumnIndex(C.ID_IMAGE)
                val nameC = cursor.getColumnIndex(C.NAME_IMAGE)
                val pathC = cursor.getColumnIndex(C.DATA_IMAGE)
                val albumC = cursor.getColumnIndex(C.ALBUM_IMAGE)
                val dateModifiedC = cursor.getColumnIndex(C.DATEM_IMAGE)
                val dateTakenC = cursor.getColumnIndex(C.DATET_IMAGE)
                val sizeC = cursor.getColumnIndex(C.SIZE_IMAGE)
                val widthC = cursor.getColumnIndex(C.WIDTH_IMAGE)
                val heightC = cursor.getColumnIndex(C.HEIGHT_IMAGE)


                while (cursor.moveToNext()) {
                    val id = ContentUris.withAppendedId(C.EXTERNAL_IMAGE, cursor.getLong(IDC))
                    val name = cursor.getString(nameC)
                    val path = cursor.getString(pathC)
                    val album = cursor.getString(albumC) ?: ".root"
                    val dateM = Date(cursor.getLong(dateModifiedC))
                    val dateT = Date(cursor.getLong(dateTakenC))
                    val size = cursor.getLong(sizeC)
                    val res = cursor.getString(widthC) + "x" + cursor.getString(heightC)


                    val media =
                        Media(
                            id,
                            name,
                            path,
                            album,
                            dateM,
                            dateT,
                            size,
                            res,
                            MediaType.Image
                        )
                    mediaList.add(media)
                }
                cursor.close()
            }
        } catch (e: Exception) {
            e.stackTrace
        }
        return mediaList
    }


    private fun getVideos(cursor: Cursor?): ArrayList<Media> {
        val mediaList = ArrayList<Media>()
        try {
            if (cursor != null) {
                val IDC = cursor.getColumnIndex(C.ID_VIDEO)
                val nameC = cursor.getColumnIndex(C.NAME_VIDEO)
                val pathC = cursor.getColumnIndex(C.DATA_VIDEO)
                val albumC = cursor.getColumnIndex(C.ALBUM_VIDEO)
                val dateModifiedC = cursor.getColumnIndex(C.DATEM_VIDEO)
                val dateTakenC = cursor.getColumnIndex(C.DATET_VIDEO)
                val sizeC = cursor.getColumnIndex(C.SIZE_VIDEO)
                val resC = cursor.getColumnIndex(C.RES_VIDEO)

                while (cursor.moveToNext()) {
                    val id = ContentUris.withAppendedId(C.EXTERNAL_VIDEO, cursor.getLong(IDC))
                    val name = cursor.getString(nameC)
                    val path = cursor.getString(pathC)
                    val album = cursor.getString(albumC) ?: ".root"
                    val dateM = Date(cursor.getLong(dateModifiedC))
                    val dateT = Date(cursor.getLong(dateTakenC))
                    val size = cursor.getLong(sizeC)
                    val res = cursor.getString(resC)


                    val media =
                        Media(
                            id,
                            name,
                            path,
                            album,
                            dateM,
                            dateT,
                            size,
                            res,
                            MediaType.Video
                        )
                    mediaList.add(media)
                }
                cursor.close()
            }
        } catch (e: Exception) {
            e.stackTrace
        }
        return mediaList
    }


    private fun imageLastStand(uri: Uri): ArrayList<Media> {
        val cursor: Cursor?

        val resolver = getApplication<DarkGalleryApp>().contentResolver

        val projection = arrayOf(
            C.ID_IMAGE,
            C.NAME_IMAGE,
            C.DATEM_IMAGE,
            C.ALBUM_IMAGE,
            C.SIZE_IMAGE,
            C.DATET_IMAGE,
            C.DATA_IMAGE,
            C.HEIGHT_IMAGE,
            C.WIDTH_IMAGE,
        )

        cursor = resolver.query(
            uri,
            projection,
            null,
            null,
            null
        )

        val mediaList = ArrayList<Media>()
        try {
            if (cursor != null) {
                val IDC = cursor.getColumnIndex(C.ID_IMAGE)
                val nameC = cursor.getColumnIndex(C.NAME_IMAGE)
                val pathC = cursor.getColumnIndex(C.DATA_IMAGE)
                val albumC = cursor.getColumnIndex(C.ALBUM_IMAGE)
                val dateModifiedC = cursor.getColumnIndex(C.DATEM_IMAGE)
                val dateTakenC = cursor.getColumnIndex(C.DATET_IMAGE)
                val sizeC = cursor.getColumnIndex(C.SIZE_IMAGE)
                val widthC = cursor.getColumnIndex(C.WIDTH_IMAGE)
                val heightC = cursor.getColumnIndex(C.HEIGHT_IMAGE)


                while (cursor.moveToNext()) {
                    val id = ContentUris.withAppendedId(C.EXTERNAL_IMAGE, cursor.getLong(IDC))
                    val name = cursor.getString(nameC)
                    val path = cursor.getString(pathC)
                    val album = cursor.getString(albumC) ?: ".root"
                    val dateM = Date(cursor.getLong(dateModifiedC))
                    val dateT = Date(cursor.getLong(dateTakenC))
                    val size = cursor.getLong(sizeC)
                    val res = cursor.getString(widthC) + "x" + cursor.getString(heightC)


                    val media =
                        Media(
                            id,
                            name,
                            path,
                            album,
                            dateM,
                            dateT,
                            size,
                            res,
                            MediaType.Image
                        )
                    mediaList.add(media)
                }
                cursor.close()
            }
        } catch (e: Exception) {
            if (mediaList.size == 0)
                mediaList.add(Media(uri, "", "", "", Date(), Date(), 0, "", MediaType.Image))
        }

        return mediaList
    }


    private fun videoLastStand(uri: Uri): ArrayList<Media> {
        val cursor: Cursor?

        val resolver = getApplication<DarkGalleryApp>().contentResolver

        val projection = arrayOf(
            C.ID_VIDEO,
            C.NAME_VIDEO,
            C.DATEM_VIDEO,
            C.ALBUM_VIDEO,
            C.SIZE_VIDEO,
            C.DATET_VIDEO,
            C.DATA_VIDEO,
            C.RES_VIDEO
        )


        cursor = resolver.query(
            uri,
            projection,
            null,
            null,
            null
        )

        val mediaList = ArrayList<Media>()

        try {
            if (cursor != null) {
                val IDC = cursor.getColumnIndex(C.ID_VIDEO)
                val nameC = cursor.getColumnIndex(C.NAME_VIDEO)
                val pathC = cursor.getColumnIndex(C.DATA_VIDEO)
                val albumC = cursor.getColumnIndex(C.ALBUM_VIDEO)
                val dateModifiedC = cursor.getColumnIndex(C.DATEM_VIDEO)
                val dateTakenC = cursor.getColumnIndex(C.DATET_VIDEO)
                val sizeC = cursor.getColumnIndex(C.SIZE_VIDEO)
                val resC = cursor.getColumnIndex(C.RES_VIDEO)

                while (cursor.moveToNext()) {
                    val id = ContentUris.withAppendedId(C.EXTERNAL_VIDEO, cursor.getLong(IDC))
                    val name = cursor.getString(nameC)
                    val path = cursor.getString(pathC)
                    val album = cursor.getString(albumC) ?: ".root"
                    val dateM = Date(cursor.getLong(dateModifiedC))
                    val dateT = Date(cursor.getLong(dateTakenC))
                    val size = cursor.getLong(sizeC)
                    val res = cursor.getString(resC)


                    val media =
                        Media(
                            id,
                            name,
                            path,
                            album,
                            dateM,
                            dateT,
                            size,
                            res,
                            MediaType.Video
                        )
                    mediaList.add(media)
                }
                cursor.close()
            }
        } catch (e: Exception) {
            if (mediaList.size == 0)
                mediaList.add(Media(uri, "", "", "", Date(), Date(), 0, "", MediaType.Video))
        }

        return mediaList
    }


    /*private fun getType(file: File): MediaType? {
        var type: MediaType? = null
        val name = file.name

        if (name.endsWith(".jpg", true)
            || name.endsWith(".png", true)
            || name.endsWith(".webp", true)
            || name.endsWith(".bmp", true)
            || name.endsWith(".gif", true)
        )
            type = MediaType.Image
        else
            if (name.endsWith(".mp4", true)
                || name.endsWith(".3gp", true)
                || name.endsWith(".mkv", true)
                || name.endsWith(".webm", true)
                || name.endsWith(".ts", true)
            )
                type = MediaType.Video

        return type
    }


    private fun buildMedia(mediaFile: File, type: MediaType): Media {
        val name = mediaFile.name
        val path = mediaFile.path
        val size = AppUtil.getSize(mediaFile.length())
        val date = mediaFile.lastModified()

        val op = BitmapFactory.Options().also { it.inJustDecodeBounds = true }
        val bitmap = BitmapFactory.decodeFile(mediaFile.path, op)
        val res = op.outWidth.toString() + "x" + op.outHeight.toString()


        return Media(
            mediaFile.toUri(),
            name,
            path,
            mediaFile.parentFile?.name ?: "",
            mediaFile.parentFile?.path.hashCode(),
            Date(date),
            Date(),
            size,
            res,
            type
        )
    }*/


}