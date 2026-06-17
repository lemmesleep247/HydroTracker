package com.cemcakmak.hydrotracker.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream

/**
 * Utility class for handling profile image operations
 * Provides image compression, storage, and retrieval functionality
 */
object ImageUtils {
    
    private const val PROFILE_IMAGE_FILENAME = "profile_image.jpg"
    private const val PROFILE_IMAGE_WEBP_FILENAME = "profile_image.webp"
    private const val MAX_IMAGE_SIZE = 300 // Max width/height in pixels
    private const val JPEG_QUALITY = 85 // Compression quality (0-100)

    private val profileImageCache = mutableMapOf<String, Bitmap>()

    /**
     * Save an image URI to local storage with compression.
     *
     * This legacy path writes a 300×300 JPEG used during onboarding. The profile
     * settings cropper writes a higher-resolution WebP via
     * [com.cemcakmak.hydrotracker.presentation.settings.profile.crop.CropImageUtils].
     *
     * @param context Application context
     * @param imageUri URI of the image to save
     * @return Local file path if successful, null if failed
     */
    fun saveProfileImage(context: Context, imageUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            inputStream?.use { stream ->
                // Decode the image
                val originalBitmap = BitmapFactory.decodeStream(stream)
                    ?: return null

                // Apply rotation correction if needed
                val correctedBitmap = correctImageOrientation(context, imageUri, originalBitmap)

                // Compress and resize the image
                val compressedBitmap = compressImage(correctedBitmap)

                // Save to internal storage
                val file = getProfileImageJpgFile(context)
                saveBitmapToFile(compressedBitmap, file)

                // Clean up
                if (correctedBitmap != originalBitmap) {
                    correctedBitmap.recycle()
                }
                compressedBitmap.recycle()

                clearProfileImageCache()
                file.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get the legacy JPEG profile image file.
     */
    private fun getProfileImageJpgFile(context: Context): File {
        return File(context.filesDir, PROFILE_IMAGE_FILENAME)
    }

    /**
     * Get the cropped WebP profile image file.
     */
    private fun getProfileImageWebpFile(context: Context): File {
        return File(context.filesDir, PROFILE_IMAGE_WEBP_FILENAME)
    }

    /**
     * Returns the existing profile image file, preferring the WebP version and
     * falling back to the legacy JPEG file.
     */
    private fun getExistingProfileImageFile(context: Context): File? {
        val webpFile = getProfileImageWebpFile(context)
        if (webpFile.exists()) return webpFile

        val jpgFile = getProfileImageJpgFile(context)
        return if (jpgFile.exists()) jpgFile else null
    }

    /**
     * Delete all profile image files and clear the in-memory cache.
     */
    fun deleteProfileImage(context: Context): Boolean {
        return try {
            val jpgDeleted = getProfileImageJpgFile(context).deleteExisting()
            val webpDeleted = getProfileImageWebpFile(context).deleteExisting()
            clearProfileImageCache()
            jpgDeleted || webpDeleted
        } catch (_: Exception) {
            false
        }
    }

    private fun File.deleteExisting(): Boolean {
        return if (exists()) delete() else false
    }
    
    /**
     * Compress and resize image to reduce file size
     */
    private fun compressImage(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        // Calculate new dimensions
        val maxDimension = MAX_IMAGE_SIZE.toFloat()
        val ratio = if (width > height) {
            maxDimension / width
        } else {
            maxDimension / height
        }
        
        return if (ratio < 1.0f) {
            // Resize if image is larger than max dimensions
            val newWidth = (width * ratio).toInt()
            val newHeight = (height * ratio).toInt()
            bitmap.scale(newWidth, newHeight)
        } else {
            // Image is already small enough
            bitmap
        }
    }
    
    /**
     * Correct image orientation based on EXIF data
     */
    private fun correctImageOrientation(context: Context, imageUri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            inputStream?.use { stream ->
                val exif = ExifInterface(stream)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                    else -> bitmap
                }
            } ?: bitmap
        } catch (_: Exception) {
            // If we can't read EXIF data, return original bitmap
            bitmap
        }
    }
    
    /**
     * Rotate bitmap by specified degrees
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    
    /**
     * Save bitmap to file
     */
    private fun saveBitmapToFile(bitmap: Bitmap, file: File) {
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        }
    }
    
    /**
     * Load profile image as bitmap if it exists.
     * Results are cached in memory so subsequent loads are instantaneous.
     */
    fun loadProfileImageBitmap(context: Context, path: String? = null): Bitmap? {
        return try {
            val file = path?.let { File(it) } ?: getExistingProfileImageFile(context) ?: return null
            val cacheKey = file.absolutePath

            profileImageCache[cacheKey]?.let { return it }

            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)?.also { bitmap ->
                    profileImageCache[cacheKey] = bitmap
                }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Clear the in-memory profile image cache.
     * Call this after the profile picture is updated or deleted.
     */
    fun clearProfileImageCache() {
        profileImageCache.clear()
    }
    
}