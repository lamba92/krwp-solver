package com.github.lamba92.utils.concurrency

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal abstract class AbstractRWPImpl<T>(protected var initial: T) : RWPLock<T> {
    protected var readersCount = 0
    protected val resourceMutex = Mutex()
    protected val readersMutex = Mutex()
}

internal abstract class UnfairRWPImpl<T>(initial: T) : AbstractRWPImpl<T>(initial) {

    protected suspend fun readersEntryCheck() = readersMutex.withLock {
        readersCount++
        if (readersCount == 1)
            resourceMutex.lock()
    }


    protected suspend fun readersExitCheck() = readersMutex.withLock {
        readersCount--
        if (readersCount == 0)
            resourceMutex.unlock()
    }

}

internal class FavoriteReadersRWPImpl<T>(initial: T) : UnfairRWPImpl<T>(initial) {

    override suspend fun read(action: suspend (T) -> Unit) {
        readersEntryCheck()
        action(initial)
        readersExitCheck()
    }

    override suspend fun write(action: suspend (T) -> T) = resourceMutex.withLock {
        initial = action(initial)
    }

}

internal class FavoriteWritersRWPImpl<T>(initial: T) : UnfairRWPImpl<T>(initial) {

    private val writersMutex = Mutex()
    private val tryReadMutex = Mutex()
    private var writersCount = 0

    override suspend fun read(action: suspend (T) -> Unit) {
        tryReadMutex.lock()
        readersEntryCheck()
        tryReadMutex.unlock()
        action(initial)
        readersExitCheck()
    }

    override suspend fun write(action: suspend (T) -> T) {
        writersMutex.withLock {
            writersCount++
            if (writersCount == 1)
                tryReadMutex.lock()
        }
        resourceMutex.withLock {
            initial = action(initial)
        }
        writersMutex.withLock {
            writersCount--
            if (writersCount == 0) {
                tryReadMutex.unlock()
            }
        }
    }

}

internal class FairRWPImpl<T>(initial: T) : AbstractRWPImpl<T>(initial) {

    private val queueMutex = Mutex()

    override suspend fun read(action: suspend (T) -> Unit) {
        queueMutex.withLock {
            readersMutex.lock()
            if (readersCount == 0)
                resourceMutex.lock()
            readersCount++
        }
        readersMutex.unlock()

        action(initial)

        readersMutex.withLock {
            readersCount--
            if (readersCount == 0)
                resourceMutex.unlock()
        }
    }

    override suspend fun write(action: suspend (T) -> T) {
        queueMutex.withLock {
            resourceMutex.lock()
        }
        initial = action(initial)
        resourceMutex.unlock()
    }
}
