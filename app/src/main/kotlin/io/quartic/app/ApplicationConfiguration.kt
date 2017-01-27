package io.quartic.app

import android.content.Context
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule

data class ApplicationConfiguration(
        val backendBaseUrl: String,
        val enablePeriodicUpload: Boolean,
        val periodicUploadIntervalMilliseconds: Long,
        val enableLiveUpload: Boolean,
        val liveUploadIntervalMilliseconds: Long
){
    companion object {
        fun load(context: Context): ApplicationConfiguration {
            val mapper = ObjectMapper(YAMLFactory())
            mapper.registerModule(KotlinModule())
            val resource = context.resources.openRawResource(R.raw.config)
            val configs = mapper.readValue<Map<String, ApplicationConfiguration>>(resource,
                    object : TypeReference<Map<String, ApplicationConfiguration>>(){})
            return configs[BuildConfig.FLAVOR]!!
        }
     }
}
