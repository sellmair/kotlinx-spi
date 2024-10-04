# kotlinx.spi: Static, Kotlin Multiplatform ServiceLoader replacement
## Demo: https://youtu.be/hMk04PjVh9M
This prototype acts as a 'Proof of Concept' and is not yet endorsed by JetBrains or Kotlin by any means. 

## Motivation
Java's ServiceLoader is a pretty popular implementation of a generic 'Service Provider Interface.' 
This ServiceLoader (and similar mechanisms), however, have the following problems:
- They do not support Kotlin/Native
- Reflection or IO at runtime might be undesirable for some applications
- Reflection might be unsafe

This spi implementation, whilst also being multiplatform, is fully static. 
All services will be known at compile time; The corresponding IR or ByteCode will be generated to 
link them. 

Note: This does not mean that the services have to be visible to the currently compiled module. 
Each 'execution' gets associated with their own 'spi compilation' and will be provided the module at runtime.

## Implementation
Code gets compiled against the `kotlinx.spi.runtime` module which, by default, returns an empty sequence
of services. However, when the `kotlinx.spi` plugin is applied to a project, then each 'executable'
will get assigned its own compilation of a *real* `kotlinx.spi.runtime`. 

e.g. when running `jvmRun`, this execution will have its associated compilation `:compileKotlinSpiJvmMain`
which will look at the runtime classpath and generated the necessary links.

### Kotlin Native Implementation
The Kotlin/Native implementation requires one additional change to the `KotlinLibraryResolver` in order
to replace the `kotlinx.spi.runtime` module with a 'real' one. See the commit in kotlin/sellmair/spi.

## Try: playground
This kotlinx.spi implementation requires a small change in the Kotlin/Native compiler which can be found in the
`sellmair/spi` branch of Kotlin. The K/N compiler needs to be built first and provided in the
`gradle.properties` file.

Run jvm/main
```shell
cd playground
./gradlew :app:jvmRun -DmainClass=MainKt
```

Run jvm/test
```shell
cd playground
./gradlew :app:jvmTest --rerun -i
```

Run macosArm64/main
```shell
cd playground
./gradlew :app:macosRun
```