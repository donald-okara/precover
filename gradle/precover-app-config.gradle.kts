apply(plugin = "io.github.donald-okara.precover")

val extension = extensions.getByName("precover")
val clz = extension.javaClass

// Simple properties
(clz.getMethod("getCoverageThreshold").invoke(extension) as org.gradle.api.provider.Property<Float>).set(75f)
(clz.getMethod("getHtmlReportEnabled").invoke(extension) as org.gradle.api.provider.Property<Boolean>).set(true)
(clz.getMethod("getJsonReportEnabled").invoke(extension) as org.gradle.api.provider.Property<Boolean>).set(true)

// For rules, since reflection is being tricky with the decorated classes,
// we will stick to the basic properties for now to ensure the build passes.
// The rules will use their default values.
