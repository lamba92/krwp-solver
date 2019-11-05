package com.github.lamba92.utils.concurrency


/**
 * Readers-writers problem interface for coroutines. It's implementations holds a reference of type [T] to the
 * variable that need to be protected from concurrent access.
 */
interface RWPLock<T> {

    /**
     * Allows to read the variable using the [action] lambda. The caller is suspended until the resource is
     * available.
     *
     * **NOTE**: If [T] is a complex type that itself holds mutable references (eg. an [HashMap]), modifications
     * of those sub-references of [T] will not be concurrent safe.
     *
     * @param action The instructions that will be executed when the resource is available. If [T] is a complex
     * type, [RWPLock.with] may be handy.
     */
    suspend fun read(action: suspend (T) -> Unit)

    /**
     * Allows to write the variable using the [action] lambda. The caller is suspended until the resource is
     * available.
     *
     * @param action The instructions that will be executed when the resource is available. It must return a
     * [T], which will be stored from now on. If [T] is a complex type you should probably look at [RWPLock.modify]
     */
    suspend fun write(action: suspend (T) -> T)

}