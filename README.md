# Log Filter

[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.lukebemish.brainfrick?style=for-the-badge)](https://plugins.gradle.org/plugin/io.github.lukebemish.brainfrick)

This gradle plugin modifies the generated gradle wrapper to add log filtering; though generally there are better approaches to controlling logging, such as
log levels, sometimes you must use libraries which log in ways that make it hard to filter the information you need from the mass of other information logged
by the tool. This plugin can filter text out of the gradle log. The filtered text can be configured through the `logfilter` extension:

```gradle
plugins {
    id 'dev.lukebemish.logfilter' version: '<version>'
}

logfilter {
    remove ~/^\[FILTER ME].*$/
}

println '[FILTER ME] This will not be logged.'
println 'This will be logged.'
```

Then, you should regenerate your gradle wrapper:

```
./gradlew wrapper
```

Note that this will affect only the log while using the gradle wrapper - it will not affect logged text while using a standalone gradle installation, nor logged
text in your IDE if your IDE does not use the wrapper.
