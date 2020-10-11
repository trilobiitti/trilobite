# Frontend module

## Motivation

Purpose of Trilobite Frontend module is to provide interfaces for users to interact with entities.
*Note: there is no hidden supernatural meaning in previous sentence.*

Imagine the following situation:
1. You use a certain service and instead of running it's interface in the browser you decide to download it's "native" application.
    You hope to gain some performance, free some RAM.
    You hope that the native application will be lightweight enough to run in background.
2. You discover that the "native" application consumes more resources than a single browser tab with that service.
3. After some research you discover that SO-CALLED "NATIVE" APPLICATION JUST RUNS IT'S OWN BROWSER INSTANCE THAT CONSUMES 100500 GB OF YOUR PRECIOUS DDR5 RAM AND 16 OF 32 CORES OF YOUR BRAND-NEW JNTEL CORE I11 CPU.

Except (probably) some details, it sounds familiar, doesn't it?

Such situations are just a sign of a global problem: owners of such services want to have user interfaces for different platforms.
The interfaces are usually (almost) identical on different platforms.
With naive approach they could be implemented as separate applications for different platforms, thus multiplying amount of work necessary by number of platforms.
But some "smart" developers use such "elegant" approaches as placing one platform (usually a web-browser) inside another one (desktop/mobile native application) sacrificing precious resources of user machine (and of whole our planet(s)) for sake of saving time and money of some small group of stupid humans.

The purpose of The Trilobite in this case is to deal with stupidity of hummans by creating a high-level tools (language, in some sense) to describe interfaces and low level interpreters instantiating interfaces for specific platforms as efficiently as possible.

## Architecture

TBD later
