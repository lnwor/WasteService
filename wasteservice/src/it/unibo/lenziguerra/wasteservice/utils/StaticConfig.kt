package it.unibo.lenziguerra.wasteservice.utils

import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import unibo.actor22comm.utils.ColorsOut
import java.io.*
import java.util.*
import kotlin.reflect.*
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.staticProperties

object StaticConfig {
    private val emptyHook: (KMutableProperty<*>, Any) -> Any? = { _, _ -> null }

    fun setConfiguration(clazz: KClass<*>, resourceName: String) {
        setConfiguration(clazz, resourceName, emptyHook, emptyHook)
    }

    fun setConfiguration(
        clazz: KClass<*>, resourceName: String,
        beforeSaveHook: (KMutableProperty<*>, Any) -> Any?,
        afterLoadHook: (KMutableProperty<*>, Any) -> Any?
    ) {
        //Nella distribuzione resourceName è in una dir che include la bin
        try {
            ColorsOut.out("%%% setTheConfiguration from file:$resourceName")
            val reader = FileReader(resourceName)
            var writer: FileWriter? = null
            try {
                val tokener = JSONTokener(reader)
                val obj = JSONObject(tokener)
                val changed = setFields(clazz, obj, beforeSaveHook, afterLoadHook)
                if (changed) {
                    writer = FileWriter(resourceName)
                    saveConfigFile(obj, writer)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            } finally {
                if (writer != null) {
                    try {
                        writer.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            ColorsOut.outappl("Config file not found, saving default config file to $resourceName", ColorsOut.YELLOW)
            saveConfigFile(createJSONObject(clazz, beforeSaveHook), resourceName)
        }
    }

    // Per testing, se usato con FileWriter sovrascriverà il file di configurazione
    // prima della lettura
    fun setConfiguration(clazz: KClass<*>, reader: Reader?, writer: Writer) {
        //Nella distribuzione resourceName è in una dir che include la bin
        try {
            val tokener = JSONTokener(reader)
            val obj = JSONObject(tokener)
            val changed = setFields(clazz, obj, emptyHook, emptyHook)
            if (changed) {
                saveConfigFile(obj, writer)
            }
        } catch (e: JSONException) {
            ColorsOut.outerr("setConfiguration ERROR " + e.message)
        }
    }

    fun saveConfigFile(obj: JSONObject?, resourceName: String?) {
        var fw: FileWriter? = null
        try {
            fw = FileWriter(resourceName)
            saveConfigFile(obj, fw)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (fw != null) {
                try {
                    fw.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun saveConfigFile(obj: JSONObject?, writer: Writer) {
        try {
            writer.write(obj!!.toString(4))
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        } finally {
            try {
                writer.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun createJSONObject(clazz: KClass<*>, beforeSaveHook: (KMutableProperty<*>, Any) -> Any?): JSONObject? {
        return try {
            val obj = JSONObject()
            for (field in getPublicStaticFields(clazz)) {
                val value = field.getter.call(clazz.objectInstance) ?: throw Exception("StaticConfig: field ${field.name} has no value when saving")
                obj.put(field.name, beforeSaveHook(field, value)?: value)
            }
            obj
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            System.err.println("Illegal access exception when saving JSON object, shouldn't happen")
            null
        }
    }

    /**
     * @return true if the object was changed due to missing fields
     */
    private fun setFields(
        clazz: KClass<*>, loadedObject: JSONObject,
        beforeSaveHook: (KMutableProperty<*>, Any) -> Any?,
        afterLoadHook: (KMutableProperty<*>, Any) -> Any?
    ): Boolean {
        var changed = false
        for (field in getPublicStaticFields(clazz)) {
            val name = field.name
            var value: Any? = loadedObject.opt(name)
            if (value != null) {
                val hookReturn = afterLoadHook(field, value)
                hookReturn?.let { value = hookReturn }
                try {
//                    println("Debug: ${name}, ${clazz.java}, ${field.returnType}, ${value!!.javaClass}, $value")
                    // Sub map
                    if (value is JSONObject) {
                        field.setter.call(clazz.objectInstance, (value as JSONObject).toMap())
                    } else {
                        field.setter.call(clazz.objectInstance, value)
                    }
                } catch (e: IllegalAccessException) {
                    e.printStackTrace() // shouldn't happen, but jic
                }
            } else {
                try {
                    val defaultValue = field.getter.call(clazz.objectInstance) ?: throw Exception("StaticConfig: field $name needs default value")
                    loadedObject.put(name, beforeSaveHook(field, defaultValue) ?: defaultValue)
                    changed = true
                    ColorsOut.outappl("Field $name not present in config, using default", ColorsOut.ANSI_YELLOW)
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
            }
        }
        return changed
    }

    private fun getPublicStaticFields(clazz: KClass<*>): List<KMutableProperty<*>> {
        return clazz.members.filter{ it.visibility == KVisibility.PUBLIC }
            .filterIsInstance<KMutableProperty<*>>()
            .map { it as KMutableProperty<*> }
    }
}