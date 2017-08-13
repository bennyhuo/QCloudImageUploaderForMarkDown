package com.bennyhuo.qcloud

import com.bennyhuo.qcloud.utils.fromJson
import com.google.gson.Gson
import com.qcloud.cos.COSClient
import com.qcloud.cos.ClientConfig
import com.qcloud.cos.meta.InsertOnly
import com.qcloud.cos.request.UploadFileRequest
import com.qcloud.cos.sign.Credentials
import java.io.File
import java.io.FileReader
import java.util.*

/**
 * Created by benny on 8/12/17.
 */
class Uploader(val options: TaskOptions) {

    companion object {
        private val IMAGE_FILE_EXTENSITONS = arrayOf(
                "bmp", "jpeg", "jpg", "png", "tiff", "gif", "pcx", "tga", "exif", "fpx", "svg", "psd", "cdr", "pcd", "dxf", "ufo", "eps", "ai", "raw", "wmf"
        )
    }

    private val historyFile by lazy {
        val path = if (options.file.isDirectory) {
            options.file.absolutePath + "/.upload/.history.json"
        } else {
            options.file.absoluteFile.parent + "/.upload/.history.json"
        }
        File(path).apply {
            if (!parentFile.isDirectory) {
                parentFile.delete()
                parentFile.mkdir()
            }
        }
    }

    val uploadHistory by lazy {
        try {
            Gson().fromJson(FileReader(historyFile), UploadHistory::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            UploadHistory()
        }
    }

    private val client by lazy {
        val credentials = Credentials(options.appInfo.APP_ID,
                options.appInfo.APP_SECRET_ID,
                options.appInfo.APP_SECRET_KEY)
        val clientConfig = ClientConfig()
        clientConfig.setRegion(options.appInfo.REGION)
        COSClient(clientConfig, credentials)
    }

    private fun uploadDirectory(directory: File) {
        directory.listFiles()?.forEach {
            //忽略该目录
            if (it.name == ".upload") return@forEach
            if (it.isDirectory) {
                uploadDirectory(it)
            } else {
                uploadSingleFile(uploadHistory[it.absolutePath, generateRemotePath(it)])
            }
        }
    }

    private fun uploadSingleFile(uploadHistoryEntry: UploadHistoryEntry) {
        val file = File(uploadHistoryEntry.localPath)
        //只上传图片
        if(file.extension.toLowerCase() !in IMAGE_FILE_EXTENSITONS) return
        if (file.lastModified() < uploadHistoryEntry.uploadTime) {
            println("Skipped. Already uploaded. Last modified: ${Date(file.lastModified())}, uploaded: ${Date(uploadHistoryEntry.uploadTime)} ")
        } else {
            val uploadFileRequest = UploadFileRequest(options.appInfo.BUCKET, uploadHistoryEntry.remotePath, file.absolutePath)
            uploadFileRequest.isEnableShaDigest = false
            uploadFileRequest.insertOnly = InsertOnly.OVER_WRITE
            val uploadFileRet = client.uploadFile(uploadFileRequest)
            println("Upload: ${uploadHistoryEntry.localPath} -> ${uploadHistoryEntry.remotePath}, result: $uploadFileRet")
            val uploadResult: UploadResult = Gson().fromJson(uploadFileRet)
            uploadHistoryEntry.remoteUrl = uploadResult.data.source_url
            uploadHistoryEntry.uploadTime = System.currentTimeMillis()
            uploadHistory[file.absolutePath] = uploadHistoryEntry
        }
    }

    private fun generateRemotePath(file: File): String {
        return "/" + uploadHistory.id + "/" + file.relativeTo(options.file)
    }


    fun upload() {
        if (options.file.isDirectory) {
            uploadDirectory(options.file)
        } else {
            uploadSingleFile(uploadHistory[options.file.absolutePath, generateRemotePath(options.file)])
        }
        saveHistory()
    }

    private fun saveHistory() {
        historyFile.delete()
        historyFile.writeText(Gson().toJson(uploadHistory))
    }
}