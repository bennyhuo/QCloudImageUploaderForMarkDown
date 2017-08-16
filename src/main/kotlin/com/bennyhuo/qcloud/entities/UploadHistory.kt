package com.bennyhuo.qcloud.entities

import java.util.*

/**
 * Created by benny on 8/12/17.
 */
class UploadHistory(val id: String = UUID.randomUUID().toString()){
    private val entries = HashMap<String, UploadHistoryEntry>()

    operator fun get(localPath: String, remotePath: String) = entries[localPath]?: UploadHistoryEntry(localPath, remotePath)

    operator fun get(localPath: String) = entries[localPath]

    operator fun set(localPath: String, uploadHistoryEntry: UploadHistoryEntry) {
        entries[localPath] = uploadHistoryEntry
    }
}

data class UploadHistoryEntry(val localPath: String,
                              val remotePath: String,
                              var uploadTime: Long = 0L){
    var remoteUrl: String? = null
}