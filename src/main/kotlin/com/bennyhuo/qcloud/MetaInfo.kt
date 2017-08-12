package com.bennyhuo.qcloud

import com.bennyhuo.qcloud.prop.AbsProperties

/**
 * Created by benny on 8/12/17.
 */
object MetaInfo: AbsProperties("/meta.properties"){
    val version: String by prop
    val author: String by prop
    val name: String by prop
    val desc: String by prop

    override fun toString(): String {
        return "By $author, Version: $version"
    }
}