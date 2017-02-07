package io.quartic.tracker.api

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(value = ActivitySensorValue::class, name = "activity"),
        JsonSubTypes.Type(value = FusedLocationSensorValue::class, name = "fused_location")
)
interface SensorValue
