package com.bennyhuo.qcloud.prop

import java.io.File
import java.io.FileInputStream
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSuperclassOf

/**
 * Created by benny on 8/12/17.
 */
class PropertiesDelegate(val path: String) {

    val properties: Properties by lazy {
        val prop = Properties()
        try {
            javaClass.getResourceAsStream(path).use {
                prop.load(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                ClassLoader.getSystemClassLoader().getResourceAsStream(path).use {
                    prop.load(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                FileInputStream(path).use {
                    prop.load(it)
                }
            }
        }

        prop
    }

    operator fun <T> getValue(thisRef: Any, property: KProperty<*>): T {
        val value = properties[property.name]
        val classOfT = property.returnType.classifier as KClass<*>
        return if (Number::class.isSuperclassOf(classOfT)) {
            classOfT.javaObjectType.getDeclaredMethod("parse${classOfT.simpleName}", String::class.java).invoke(null, value)
        } else {
            value
        } as T
    }

    operator fun <T> setValue(thisRef: Any, property: KProperty<*>, value: T) {
        properties[property.name] = value
        File(path).outputStream().use {
            properties.store(it, "")
        }
    }
}

abstract class AbsProperties(path: String) {
    protected val prop = PropertiesDelegate(path)
}