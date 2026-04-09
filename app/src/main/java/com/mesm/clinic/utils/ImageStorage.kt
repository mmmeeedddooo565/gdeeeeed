package com.mesm.clinic.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageStorage {
    fun copyToPrivateStorage(context: Context, sourceUri: Uri): String {
        val dir = File(context.filesDir, "case_images").apply { mkdirs() }
        val target = File(dir, UUID.randomUUID().toString() + ".jpg")
        context.contentResolver.openInputStream(sourceUri).use { input ->
            FileOutputStream(target).use { output ->
                input?.copyTo(output)
            }
        }
        return Uri.fromFile(target).toString()
    }

    fun deleteStoredUri(uri: String?) {
        val path = runCatching { Uri.parse(uri).path }.getOrNull() ?: return
        runCatching { File(path).takeIf { it.exists() }?.delete() }
    }
}
