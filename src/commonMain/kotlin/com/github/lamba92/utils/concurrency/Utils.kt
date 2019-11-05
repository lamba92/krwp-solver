package com.github.lamba92.utils.concurrency

/**
 * A readers-writers problem lock for coroutines.
 * @param initial Initial value to be stored.
 * @param fairness The fairness policy to be used.
 */
@Suppress("FunctionName")
fun <T> RWPLock(initial: T, fairness: RWPFairness = RWPFairness.FAIR): RWPLock<T> = when (fairness) {
    RWPFairness.FAVOUR_WRITERS -> FavoriteWritersRWPImpl(initial)
    RWPFairness.FAVOUR_READERS -> FavoriteReadersRWPImpl(initial)
    RWPFairness.FAIR -> FairRWPImpl(initial)
}

/**
 * Wraps [this] as an [RWPLock].
 * @param fairness The fairness policy to be used.
 */
fun <T> T.asRWPLock(fairness: RWPFairness = RWPFairness.FAIR) =
    RWPLock(this, fairness)

/**
 * Allows to modify the variable using the [action] extension lambda. The caller is suspended until the resource is
 * available.
 *
 * **NOTE**: It counts as a [RWPLock.write]!
 *
 * @param action The instructions that will be executed when the resource is available.
 */
suspend fun <T> RWPLock<T>.modify(action: suspend T.() -> Unit) =
    write { it.apply { action() } }

/**
 * Allows to read the variable using the [action] extension lambda. The caller is suspended until the resource is
 * available.
 *
 * **NOTE**: It counts as a [RWPLock.read]!
 *
 * @param action The instructions that will be executed when the resource is available.
 */
suspend fun <T> RWPLock<T>.with(action: suspend T.() -> Unit) =
    read { it.apply { action() } }

/**
 * Fairness policies.
 */
enum class RWPFairness {
    FAVOUR_WRITERS, FAVOUR_READERS, FAIR
}
