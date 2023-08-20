package com.javadabl.darkgallery.util

sealed class FileResult<out T>(
    val data: T? = null,
    val Message: String? = null,
) {
    class Success<T>(data: T) : FileResult<T>(data)

    class Loading<T> : FileResult<T>()
}