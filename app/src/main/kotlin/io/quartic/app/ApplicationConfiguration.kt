package io.quartic.app

import android.content.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.quartic.app.R

data class ApplicationConfiguration(
        val backendBaseUrl: String
){
    companion object {
        fun load(context: Context): ApplicationConfiguration {
            val mapper = ObjectMapper(YAMLFactory())
            mapper.registerModule(KotlinModule())
            val resource = context.resources.openRawResource(R.raw.config)
            return mapper.readValue(resource, ApplicationConfiguration::class.java)
        }
     }
}
