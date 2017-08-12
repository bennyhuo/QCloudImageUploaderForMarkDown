package com.bennyhuo.qcloud

import java.util.*

/**
 * Created by benny on 8/12/17.
 */
class UploadHistory(val id: String = UUID.randomUUID().toString()){
    val entries = HashMap<String, UploadHistoryEntry>()
}

data class UploadHistoryEntry(val localPath: String,
                              val remotePath: String,
                              val uploadTime: Long = System.currentTimeMillis()){
    var remoteUrl: String? = null
}