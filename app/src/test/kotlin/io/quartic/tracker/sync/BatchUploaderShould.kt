package io.quartic.tracker.sync

import com.nhaarman.mockito_kotlin.*
import io.quartic.app.BuildConfig
import io.quartic.app.state.ApplicationState
import io.quartic.app.sync.BatchUploader
import io.quartic.app.sync.BatchUploader.Companion.MAX_CONSECUTIVE_AUTH_FAILURES
import io.quartic.tracker.api.SensorValue
import io.quartic.tracker.api.UploadRequest
import org.junit.Test
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.stubbing.OngoingStubbing
import retrofit2.Response.error
import retrofit2.adapter.rxjava.HttpException
import rx.Observable.error
import rx.Observable.just

class BatchUploaderShould {
    private val state = mock<ApplicationState>(RETURNS_DEEP_STUBS)
    private val getBatteryLevel = mock<() -> Int>{}
    private val getCurrentTimeMillis = mock<() -> Long>{}
    private val uploader = BatchUploader(state, getBatteryLevel, getCurrentTimeMillis)

    private val paramsA = State(listOf(sensorValue(69), sensorValue(70)), 123, 42, 5)
    private val paramsB = State(listOf(sensorValue(71), sensorValue(72)), 126, 39, 3)

    @Test
    fun do_nothing_if_user_id_not_set() {
        mockState(paramsA)
        whenever(state.userId).thenReturn(null)
        whenever(state.database.backlogSize).thenReturn(5)  // This would otherwise allow a sync to occur

        uploader.upload()

        verify(state, never()).lastAttemptedSyncTime = any()
        verify(state, never()).lastSyncTime = any()
        verify(state.authClient, never()).upload(any())
    }

    @Test
    fun do_nothing_except_log_sync_times_if_backlog_is_empty_not_set() {
        mockState()
        whenever(state.userId).thenReturn("1234")             // This would otherwise allow a sync to occur
        whenever(state.database.backlogSize).thenReturn(0)

        uploader.upload()

        verify(state).lastAttemptedSyncTime = INITIAL_TIMESTAMP
        verify(state).lastSyncTime = FINAL_TIMESTAMP
        verify(state.authClient, never()).upload(any())
    }

    @Test
    fun perform_upload_and_delete_entries_and_log_success_if_user_id_set_and_non_empty_backlog() {
        mockValidAuth()
        mockSuccessfulUpload()
        mockState(paramsA)

        uploader.upload()

        verify(state).lastAttemptedSyncTime = INITIAL_TIMESTAMP
        verify(state).lastSyncTime = FINAL_TIMESTAMP
        verify(state.authClient).upload(paramsA.toRequest())
        verify(state.database).delete(listOf(69, 70))
    }

    @Test
    fun keep_uploading_until_backlog_is_empty() {
        mockValidAuth()
        mockSuccessfulUpload()
        mockState(paramsA, paramsB)

        uploader.upload()

        verify(state.authClient).upload(paramsA.toRequest())
        verify(state.authClient).upload(paramsB.toRequest())
    }

    @Test
    fun stop_looping_and_not_record_success_if_backlog_fails() {
        mockValidAuth()
        mockUnsuccessfulUpload(500)
        mockState(paramsA, paramsB)

        uploader.upload()

        verify(state, never()).lastSyncTime = any()
        verify(state.authClient, times(1)).upload(any())
    }

    @Test
    fun not_delete_entries_if_upload_fails() {
        mockValidAuth()
        mockUnsuccessfulUpload(500)
        mockState(paramsA)

        uploader.upload()

        verify(state.database, never()).delete(any())
    }

    @Test
    fun increment_failure_count_if_auth_error() {
        mockValidAuth()
        mockUnsuccessfulUpload(401)
        mockState(paramsA)
        whenever(state.numConsecutiveSyncAuthFailures).thenReturn(1)

        uploader.upload()

        verify(state).numConsecutiveSyncAuthFailures = 2
    }

    @Test
    fun not_increment_failure_count_if_other_error() {
        mockValidAuth()
        mockUnsuccessfulUpload(403)
        mockState(paramsA)

        uploader.upload()

        verify(state, never()).numConsecutiveSyncAuthFailures = any()
    }

    @Test
    fun clear_user_id_if_failure_count_reaches_limit() {
        mockValidAuth()
        mockUnsuccessfulUpload(401)
        mockState(paramsA)
        whenever(state.numConsecutiveSyncAuthFailures).thenReturn(MAX_CONSECUTIVE_AUTH_FAILURES)    // Should be MAX - 1, but this mock always returns the same value

        uploader.upload()

        verify(state).userId = null
    }

    @Test
    fun reset_failure_count_if_upload_successful() {
        mockValidAuth()
        mockSuccessfulUpload()
        mockState(paramsA)

        uploader.upload()

        verify(state).numConsecutiveSyncAuthFailures = 0
    }

    private fun mockValidAuth() {
        whenever(state.userId).thenReturn("1234")
    }

    private fun mockSuccessfulUpload() {
        whenever(state.authClient.upload(any())).thenReturn(just(1))    // Returned int isn't used currently
    }

    private fun mockUnsuccessfulUpload(code: Int) {
        whenever(state.authClient.upload(any())).thenReturn(error(HttpException(error<String>(code, mock()))))
    }

    private fun mockState(vararg states: State) {
        fun <T> stubReturns(mock: T, values: List<T>) {
            if (!values.isEmpty()) {
                values.fold(whenever(mock), OngoingStubbing<T>::thenReturn)
            }
        }

        stubReturns(state.database.sensorValues, states.map { it.values })
        stubReturns(getCurrentTimeMillis(), listOf(INITIAL_TIMESTAMP)
                + states.map { it.timestamp }
                + listOf(FINAL_TIMESTAMP)
        )
        stubReturns(getBatteryLevel(), states.map { it.batteryLevel })
        stubReturns(state.database.backlogSize, states
                .map { it.backlogSize }
                .flatMap { listOf(it, it) }
                + listOf(0)                      // Last value to cause termination
        )
    }

    private data class State(
            val values: List<SensorValue>,
            val timestamp: Long,
            val backlogSize: Int,
            val batteryLevel: Int
    ) {
        fun toRequest() = UploadRequest(
                timestamp = timestamp,
                appVersionCode = BuildConfig.VERSION_CODE,
                appVersionName = BuildConfig.VERSION_NAME,
                batteryLevel = batteryLevel,
                backlogSize = backlogSize,
                values = values
        )
    }

    private fun sensorValue(myId: Int) = mock<SensorValue> { on { id } doReturn myId }

    companion object {
        val INITIAL_TIMESTAMP = 666L
        val FINAL_TIMESTAMP = 777L
    }
}