package com.bennyhuo.qcloud.utils

import com.google.gson.Gson

/**
 * Created by benny on 8/12/17.
 */
inline fun <reified T> Gson.fromJson(json: String)
    = fromJson(json, T::class.java)