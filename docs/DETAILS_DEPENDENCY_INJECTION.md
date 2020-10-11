# Dependency injection

The Trilobite DI container is inspired by the one implemented in [SmartActors](https://github.com/SmartTools/smartactors-core).
The DI container is available from any point of code as a global singleton service locator called, suddenly, [DI](../dna/src/commonMain/kotlin/di/di.kt).
Yet the actual instance of container can be different for different coroutines, thus allowing registration different dependencies for different contexts.
For example a "currentRequest" dependency (resolving to HTTP request being handled by the coroutine) can be registered in a container created when an inbound HTTP request is received.
In the most advanced case there may exist few separate Trilobitish applications in a single process with the only technical difference being different DI containers used in coroutines of different applications.

## Dependency registration and resolution

Dependencies are registered by associating a function with certain key:

```kotlin
DI.register("hello") { args -> "Hello, ${args[0]}!" }
```

The function receives array of arguments typed as `Array<out Any?>`.

Then the function can be invoked:

```kotlin
DI.resolve("hello", "Trilobite") // > "Hello, Trilobite!"
```

or more verbosely

```kotlin
DI.getResolver("hello")(arrayOf("Trilobite"))
```

You may think of it as of an overcomplicated way to declare and invoke a function.
And with [`bindDependency()`](../dna/src/commonMain/kotlin/di/diHelpers.kt) helpers it looks even more like that:

```kotlin
val sayHello = bindDependency<String, String>("hello") { it ->
    // Optional default resolve implementation.
    // Will be used when no other resolver is
    // registered for given key.
    "Hello, $it!"
}

sayHello("Trilobite") // > "Hello, Trilobite!"
```

with the only difference that dependencies can be overridden for current container:

```kotlin
sayHello.use { it ->
    "Hola, $it!"
}

sayHello("Trilobite") // > "Hola, Trilobite!"
```

There also is a way to bind dependency to a variable using a property delegate:

```kotlin
var greeting: String by bindDependency("greeting") { "Hello," }

// Somewhere in a fork of your application made by russian hackers:
greeting = "Здравья желаю, товарищ"
```

## Coroutine-locality

**Note: coroutine-local DI container is currently implemented in a potentially incorrect unsafe and inefficient way. Thus it's not recommended to use `DIContextElement` directly.**

To make a coroutine run with specific DI instance `withDI` coroutine builder should be used to create it:

```kotlin
val myDI = PlainDIContainer(/* ... */)

GlobalScope.run {
    withDI(myDI) {
        assert(myDI === DI.instance)

        launch {
            // suspending coroutine code goes here...
            assert(myDI === DI.instance)
        }
    }
}
```

There also is a global `withDI` function that just executes a non-suspending block setting provided DI instance as current and resetting previously current instance afterward, so be careful not to mix up those two.

## Hierarchy

DI containers are hierarchical.
In the simplest form, a single container may inherit dependencies from just one another container:

```kotlin
val parent = PlainDIContainer()
val child = PlainDIContainer(parent = parent)

parent.register("1") { "1p" }
parent.register("2") { "2p" }

child.register("2") { "2c" }
child.register("3") { "3c" }

parent.resolve("1") // > "1p"
parent.resolve("2") // > "2p"
parent.resolve("3") // > (error)

child.resolve("1") // > "1p"
child.resolve("2") // > "2c"
child.resolve("3") // > "3c"
```

Dependencies in child container can be overridden but not overwritten.
