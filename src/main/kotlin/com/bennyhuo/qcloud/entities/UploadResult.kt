package com.bennyhuo.qcloud.entities

/**
 * Created by benny on 8/12/17.
 */
data class UploadResult(var code: Int,
                        var message: String,
                        var request_id: String,
                        var data: Data) {
    data class Data(var access_url: String,
                    var resource_path: String,
                    var source_url: String,
                    var url: String,
                    var vid: String)
}