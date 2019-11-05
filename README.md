# READERS- WRITERS PROBLEM IMPLEMENTATION WITH KOTLIN COROUTINES

Well that's a long title for a 3 file project!

This library allows to wrap an object and manage access to it in an asynchronous context.

The implementation follows this [Wikipedia article](https://en.wikipedia.org/wiki/Readers%E2%80%93writers_problem).

## How does it work
It's pretty straight forward:
```kotlin
suspend fun main() = coroutineScope {
    val myAtomicReference = RWPLock(42, RWPFairness.FAVOUR_WRITERS)

    (0..10).map { times ->
        launch {
            myAtomicReference.write { it + 1 }
            delay(Random.nextLong(200, 300))
            myAtomicReference.read { println("#$times - values is: $it") }
        }
    }
        .map { it.join() }
    
    return@coroutineScope

}
```

The default fairness uses a queue to to ensure it. Pro-readers or pro-writers policies are available as well.

## Download  [ ![Download](https://api.bintray.com/packages/lamba92/com.github.lamba92/krwp-solver/images/download.svg) ](https://bintray.com/lamba92/com.github.lamba92/krwp-solver/_latestVersion)
Use Gradle or Maven:
```kotlin
implementation("com.github.lamba92:krw-problem:0.0.1")
```

And don't forget to add `jcenter()` as repository for dependecies!
