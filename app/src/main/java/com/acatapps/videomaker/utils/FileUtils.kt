package com.acatapps.videomaker.utils

import android.graphics.Bitmap
import android.os.Environment
import android.view.View
import com.acatapps.videomaker.BuildConfig
import com.acatapps.videomaker.R
import com.acatapps.videomaker.application.VideoMakerApplication
import java.io.*

object FileUtils {
    val internalPath = VideoMakerApplication.getContext().getExternalFilesDir(null)
    private val txtTempFolderPath = "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/tempText"
    private val videoTempFolder = "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/tempvideo"
    private val tempRecordAudioFolder = "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/tempRecordAudio"
    val themeFolderPath = "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/theme"
    val audioDefaultFolderPath = "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/audio"
    val defaultAudio = "$audioDefaultFolderPath/default_bg_sound.mp3"
    private val musicTempDataFolderPath = "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/musicTempData"
    private val stickerTempFolderPath = "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/stickerTemp"
     val outputFolderPath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}/${VideoMakerApplication.getContext().getString(R.string.app_name)}"//"$internalPath/DCIM/${VideoMakerApplication.getContext().getString(R.string.app_name)}"
    val myStuioFolderPath get() = outputFolderPath


    val tempImageFolderPath = "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/tempImage"

    fun saveBitmapToTempData(bitmap: Bitmap?): String {
        val tempDataFolderPath = tempImageFolderPath
        val tempDataFolder = File(tempDataFolderPath)
        if(!tempDataFolder.exists()) {
            tempDataFolder.mkdirs()
        }
        val outFile = File("$tempDataFolderPath/${System.currentTimeMillis()}${View.generateViewId()}")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(outFile)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return outFile.absolutePath
    }

    fun saveBitmapToTempData(bitmap: Bitmap?, outFileName:String): String {
        val tempDataFolderPath = tempImageFolderPath
        val tempDataFolder = File(tempDataFolderPath)
            tempDataFolder.mkdirs()
        val outFile = File("$tempDataFolderPath/$outFileName")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(outFile)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return outFile.absolutePath
    }

    fun deleteTempFolder() {
        Thread{
            val internalPath = VideoMakerApplication.getContext().getExternalFilesDir(null)
            val tempDataFolderPath = "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/tempdata"
            val tempDataFolder = File(tempDataFolderPath)
            if(tempDataFolder.exists() && tempDataFolder.isDirectory) {
                for (file in tempDataFolder.listFiles()) {
                    file.delete()
                }
            }
        }.start()
    }
    fun getTempMp3OutPutFile():String {
        File(musicTempDataFolderPath).mkdirs()
        return "$musicTempDataFolderPath/audio_${System.currentTimeMillis()}.mp4"
    }
    fun getTempAudioOutPutFile(fileType:String):String {
        File(musicTempDataFolderPath).mkdirs()
        return "$musicTempDataFolderPath/audio_${System.currentTimeMillis()}.$fileType"
    }

    fun saveStickerToTemp(bitmap:Bitmap):String {
        File(stickerTempFolderPath).mkdirs()

        val outFile = File("$stickerTempFolderPath/sticker_${System.currentTimeMillis()}")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(outFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return outFile.absolutePath
    }

    fun getTempVideoPath():String {
        File(videoTempFolder).mkdirs()
        return "$videoTempFolder/video-temp-${System.currentTimeMillis()}.mp4"
    }
    fun getTempM4aAudioPath():String {
        File(videoTempFolder).mkdirs()
        return "$videoTempFolder/video-temp-${System.currentTimeMillis()}.mp3"
    }


    fun getOutputVideoPath():String {
        File(outputFolderPath).mkdirs()
        return "$outputFolderPath/video-${System.currentTimeMillis()}.mp4"
    }

    fun getOutputVideoPath(size:Int):String {
        File(outputFolderPath).mkdirs()
        return "$outputFolderPath/video-${System.currentTimeMillis()}-$size.mp4"
    }



    fun clearTemp() {
        val tempMusicFolder = File(musicTempDataFolderPath)
        try {
            if(tempMusicFolder.exists() && tempMusicFolder.isDirectory &&tempMusicFolder.listFiles() != null) {
                for(file in tempMusicFolder.listFiles()) {
                    file.delete()
                }
            }

        }catch (e:java.lang.Exception) {

        }

        val tempVideoFolder = File(videoTempFolder)
        try {
            if(tempVideoFolder.exists() && tempVideoFolder.isDirectory && tempVideoFolder.listFiles() != null) {
                for(file in tempVideoFolder.listFiles()) {
                    file.delete()
                }
            }

        }catch (e:java.lang.Exception) {

        }

        val tempStickerFolder = File(stickerTempFolderPath)
        try {
            if(tempStickerFolder.exists() && tempStickerFolder.isDirectory && tempStickerFolder.listFiles() != null) {
                for(file in tempStickerFolder.listFiles()) {
                    file.delete()
                }
            }

        }catch (e:java.lang.Exception) {

        }

        val tempImageFolder = File(tempImageFolderPath)
        try {
            if(tempImageFolder.exists() && tempImageFolder.isDirectory && tempImageFolder.listFiles() != null) {
                for(file in tempImageFolder.listFiles()) {
                    file.delete()
                }
            }

        }catch (e:java.lang.Exception) {

        }

    }

    fun getTextTempOutFile():String {
        File(txtTempFolderPath).mkdirs()
        return "$txtTempFolderPath/${System.currentTimeMillis()}.txt"
    }


    fun writeTextListFile(filePathList:ArrayList<String>) :String{
        val txtOutFilePath = getTextTempOutFile()
        val outFile = File(txtOutFilePath)
        try {
            val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(outFile)))
            for(path in filePathList) {
                writer.write("file '${path}'\n")
            }
            writer.close()
        } catch (e:java.lang.Exception) {

        }

        return outFile.absolutePath

    }

    fun getAudioRecordTempFilePath():String {
        File(tempRecordAudioFolder).mkdirs()
        return "${tempRecordAudioFolder}/record_${System.currentTimeMillis()}.3gp"
    }

    fun copyFileTo(inPath:String, outPath:String) {
        val inputStream = FileInputStream(File(inPath))
        val outputStream = FileOutputStream(File(outPath))
        copyFile(inputStream, outputStream)
    }
    private fun copyFile(inputStream: InputStream, outputStream: FileOutputStream) {
        val buffer = ByteArray(1024)
        var read:Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
    }

    fun deleteFile(pathList:ArrayList<String>) {
        for(path in pathList) {
            val file = File(path)
            if(file.exists()) file.delete()
        }
    }


}