apply(plugin = "io.github.donald-okara.precover.root")

val extension = extensions.getByName("precoverRoot")
val method = extension.javaClass.getMethod("getAggregateCoverageThreshold")
val property = method.invoke(extension) as org.gradle.api.provider.Property<Float>
property.set(80f)
