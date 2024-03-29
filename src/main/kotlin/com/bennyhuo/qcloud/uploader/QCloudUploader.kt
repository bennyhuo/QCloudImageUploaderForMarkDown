package com.bennyhuo.qcloud.uploader

import com.bennyhuo.qcloud.entities.TaskOptions
import com.bennyhuo.qcloud.entities.UploadHistory
import com.bennyhuo.qcloud.entities.UploadResult
import com.bennyhuo.qcloud.utils.fromJson
import com.bennyhuo.qcloud.utils.logger
import com.google.gson.Gson
import com.qcloud.cos.COSClient
import com.qcloud.cos.ClientConfig
import com.qcloud.cos.meta.InsertOnly
import com.qcloud.cos.request.UploadFileRequest
import com.qcloud.cos.sign.Credentials
import java.io.File
import java.io.FileReader
import java.lang.RuntimeException
import java.util.*

/**
 * Created by benny on 8/12/17.
 */
class QCloudUploader(val options: TaskOptions) {

    companion object {
        private val IMAGE_FILE_EXTENSIONS = arrayOf(
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
                if(options.removeAfterUploading){
                    logger.debug("Try Remove local directory: ${it.path}")
                    it.delete()
                }
            } else {
                uploadSingleFile(it)
            }
        }
    }

    private fun uploadSingleFile(file: File) {
        val uploadHistoryEntry = if (file == options.file){
            uploadHistory[file.name, generateRemotePath(file)]
        }else{
            uploadHistory[file.toRelativeString(options.file), generateRemotePath(file)]
        }
        //只上传图片
        if (file.extension.toLowerCase() !in IMAGE_FILE_EXTENSIONS) return
        if (file.lastModified() < uploadHistoryEntry.uploadTime) {
            logger.debug("Skipped ${file.path}. Already uploaded. Last modified: ${Date(file.lastModified())}, uploaded: ${Date(uploadHistoryEntry.uploadTime)} ")
        } else {
            val uploadFileRequest = UploadFileRequest(options.appInfo.BUCKET, uploadHistoryEntry.remotePath, file.absolutePath)
            uploadFileRequest.isEnableShaDigest = false
            uploadFileRequest.insertOnly = InsertOnly.OVER_WRITE
            val uploadFileRet = client.uploadFile(uploadFileRequest)
            logger.debug("Upload: ${uploadHistoryEntry.localPath} -> ${uploadHistoryEntry.remotePath}, result: $uploadFileRet")
            val uploadResult: UploadResult = Gson().fromJson(uploadFileRet)
            if(uploadResult.code == 0){
                uploadHistoryEntry.remoteUrl = if(uploadResult.data.source_url.startsWith("http://")){
                    uploadResult.data.source_url.replace("http://", "https://")
                } else {
                    uploadResult.data.source_url
                }
                uploadHistoryEntry.uploadTime = System.currentTimeMillis()
                uploadHistory[uploadHistoryEntry.localPath] = uploadHistoryEntry
                if(options.removeAfterUploading){
                    logger.debug("Remove local file: ${file.path}")
                    file.delete()
                }
            } else {
                logger.error("Upload: ${uploadHistoryEntry.localPath} -> ${uploadHistoryEntry.remotePath}, failed: result: $uploadFileRet")
                throw RuntimeException("Upload failed. ")
            }
        }
    }

    private fun generateRemotePath(file: File): String {
        return "/" + uploadHistory.id + "/" + if (file == options.file)
            file.name
        else
            file.toRelativeString(options.file).replace("\\", "/")
    }


    fun upload() {
        if (options.file.isDirectory) {
            uploadDirectory(options.file)
        } else {
            uploadSingleFile(options.file)
        }
        saveHistory()
    }

    private fun saveHistory() {
        val backupFile = File(historyFile.parent, historyFile.name + ".bak")
        if(backupFile.exists()){
            backupFile.delete()
        }
        historyFile.renameTo(backupFile)
        logger.info("History file is backed up to $backupFile")
        historyFile.writeText(Gson().toJson(uploadHistory))
    }
}
