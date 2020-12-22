
Apache Velocity convenience plugin.

Instructions for applying the plugin are at

https://plugins.gradle.org/plugin/com.brambolt.gradle.velocity

A repository declaration for Bintray is also needed, in a snippet like this in
the project's `settings.gradle` (where `bramboltVersion` is the version in use):

```
pluginManagement {
  repositories {
    gradlePluginPortal()
    maven { url 'https://dl.bintray.com/brambolt/public' }
  }
  plugins {
    id 'com.brambolt.gradle.velocity' version bramboltVersion
  }
}
```

The sample project (in `samples/simple`) shows how the plugin can be 
configured to instantiate templates using the Velocity engine.