package io.quartic.app.sensors

import io.quartic.app.model.LocationUpdate
import rx.Observable

interface LocationProvider {
    fun get(): Observable<LocationUpdate>
}
