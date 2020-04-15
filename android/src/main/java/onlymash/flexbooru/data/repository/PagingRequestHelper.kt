package onlymash.flexbooru.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

class PagingRequestHelper {

    private val mutex = Mutex()

    private val requestQueues =
        arrayOf(
            RequestQueue(RequestType.INITIAL),
            RequestQueue(RequestType.BEFORE),
            RequestQueue(RequestType.AFTER)
        )
    private val listeners = CopyOnWriteArrayList<Listener>()

    /**
     * Adds a new listener that will be notified when any request changes [state][Status].
     *
     * @param listener The listener that will be notified each time a request's status changes.
     * @return True if it is added, false otherwise (e.g. it already exists in the list).
     */
    fun addListener(listener: Listener): Boolean = listeners.add(listener)

    /**
     * Removes the given listener from the listeners list.
     *
     * @param listener The listener that will be removed.
     * @return True if the listener is removed, false otherwise (e.g. it never existed)
     */
    fun removeListener(listener: Listener): Boolean = listeners.remove(listener)

    /**
     * Runs the given [Callback] if no other requests in the given request type is already
     * running.
     *
     *
     * If run, the request will be run in the current thread.
     *
     * @param type    The type of the request.
     * @param handleCallback The request to run.
     * @return True if the request is run, false otherwise.
     */
    suspend fun runIfNotRunning(
        type: RequestType,
        handleCallback: (Callback) -> Unit
    ): Boolean {
        val hasListeners = !listeners.isEmpty()
        var report: StatusReport? = null
        mutex.withLock {
            val queue =
                requestQueues[type.ordinal]
            if (queue.running != null) {
                return false
            }
            queue.running = handleCallback
            queue.status = Status.RUNNING
            queue.failed = null
            queue.lastError = null
            if (hasListeners) {
                report = prepareStatusReportLocked()
            }
        }
        report?.dispatchReport()
        RequestWrapper(handleCallback, this, type).run()
        return true
    }

    private fun prepareStatusReportLocked(): StatusReport {
        val errors = arrayOf(
            requestQueues[0].lastError,
            requestQueues[1].lastError,
            requestQueues[2].lastError
        )
        return StatusReport(
            getStatusForLocked(RequestType.INITIAL),
            getStatusForLocked(RequestType.BEFORE),
            getStatusForLocked(RequestType.AFTER),
            errors
        )
    }

    private fun getStatusForLocked(type: RequestType): Status {
        return requestQueues[type.ordinal].status
    }

    suspend fun recordResult(
        wrapper: RequestWrapper,
        throwable: Throwable?
    ) {
        var report: StatusReport? = null
        val success = throwable == null
        val hasListeners = !listeners.isEmpty()
        mutex.withLock {
            val queue =
                requestQueues[wrapper.type.ordinal]
            queue.running = null
            queue.lastError = throwable
            if (success) {
                queue.failed = null
                queue.status = Status.SUCCESS
            } else {
                queue.failed = wrapper
                queue.status = Status.FAILED
            }
            if (hasListeners) {
                report = prepareStatusReportLocked()
            }
        }
        report?.dispatchReport()
    }

    private fun StatusReport.dispatchReport() {
        for (listener in listeners) {
            listener.onStatusChange(this)
        }
    }

    /**
     * Retries all failed requests.
     *
     * @return True if any request is retried, false otherwise.
     */
    suspend fun retryAllFailed(): Boolean {
        val toBeRetried = arrayOfNulls<RequestWrapper>(RequestType.values().size)
        var retried = false
        mutex.withLock {
            for (i in RequestType.values().indices) {
                toBeRetried[i] = requestQueues[i].failed
                requestQueues[i].failed = null
            }
        }
        for (failed in toBeRetried) {
            if (failed != null) {
                failed.retry()
                retried = true
            }
        }
        return retried
    }

    class RequestWrapper(
        val handleCallback: (Callback) -> Unit,
        val helper: PagingRequestHelper,
        val type: RequestType
    ) {

        suspend fun run() {
            withContext(Dispatchers.IO) {
                handleCallback(Callback(this@RequestWrapper, helper))
            }
        }

        suspend fun retry() {
            withContext(Dispatchers.IO) {
                helper.runIfNotRunning(type, handleCallback)
            }
        }

    }

    class Callback (
        private val wrapper: RequestWrapper,
        private val helper: PagingRequestHelper
    ) {
        private val called = AtomicBoolean()

        /**
         * Call this method when the request succeeds and new data is fetched.
         */
        suspend fun recordSuccess() {
            if (called.compareAndSet(false, true)) {
                helper.recordResult(wrapper, null)
            } else {
                throw IllegalStateException(
                    "already called recordSuccess or recordFailure"
                )
            }
        }

        /**
         * Call this method with the failure message and the request can be retried via
         * [.retryAllFailed].
         *
         * @param throwable The error that occured while carrying out the request.
         */
        suspend fun recordFailure(throwable: Throwable) {
            if (called.compareAndSet(false, true)) {
                helper.recordResult(wrapper, throwable)
            } else {
                throw IllegalStateException(
                    "already called recordSuccess or recordFailure"
                )
            }
        }

    }

    /**
     * Data class that holds the information about the current status of the ongoing requests
     * using this helper.
     */
    class StatusReport internal constructor(
        /**
         * Status of the latest request that were submitted with [RequestType.INITIAL].
         */
        val initial: Status,
        /**
         * Status of the latest request that were submitted with [RequestType.BEFORE].
         */
        val before: Status,
        /**
         * Status of the latest request that were submitted with [RequestType.AFTER].
         */
        val after: Status,
        private val errors: Array<Throwable?>
    ) {

        /**
         * Convenience method to check if there are any running requests.
         *
         * @return True if there are any running requests, false otherwise.
         */
        fun hasRunning(): Boolean {
            return initial == Status.RUNNING || before == Status.RUNNING || after == Status.RUNNING
        }

        /**
         * Convenience method to check if there are any requests that resulted in an error.
         *
         * @return True if there are any requests that finished with error, false otherwise.
         */
        fun hasError(): Boolean {
            return initial == Status.FAILED || before == Status.FAILED || after == Status.FAILED
        }

        /**
         * Returns the error for the given request type.
         *
         * @param type The request type for which the error should be returned.
         * @return The [Throwable] returned by the failing request with the given type or
         * `null` if the request for the given type did not fail.
         */
        fun getErrorFor(type: RequestType): Throwable? {
            return errors[type.ordinal]
        }

        override fun toString(): String {
            return ("StatusReport{"
                    + "initial=" + initial
                    + ", before=" + before
                    + ", after=" + after
                    + ", errors=" + errors.contentToString()
                    + '}')
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val that = other as StatusReport
            if (initial != that.initial) return false
            if (before != that.before) return false
            return if (after != that.after) false else errors.contentEquals(that.errors)
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
        }

        override fun hashCode(): Int {
            var result = initial.hashCode()
            result = 31 * result + before.hashCode()
            result = 31 * result + after.hashCode()
            result = 31 * result + errors.contentHashCode()
            return result
        }

    }

    /**
     * Listener interface to get notified by request status changes.
     */
    interface Listener {
        /**
         * Called when the status for any of the requests has changed.
         *
         * @param report The current status report that has all the information about the requests.
         */
        fun onStatusChange(report: StatusReport)
    }

    /**
     * Available request types.
     */
    enum class RequestType {
        INITIAL,
        BEFORE,
        AFTER
    }

    internal inner class RequestQueue(val requestType: RequestType) {
        var failed: RequestWrapper? = null
        var running: ((Callback) -> Unit)? = null
        var lastError: Throwable? = null
        var status = Status.SUCCESS
    }
}