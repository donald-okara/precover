package io.github.donald_okara.precover.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import io.github.donald_okara.precover.core.models.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

class PrecoverProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {

    private val metadataList = mutableListOf<ComposableMetadata>()
    private val sources = mutableListOf<KSFile>()
    private val previewBodyCalls = mutableMapOf<String, Set<String>>()
    private val providerScenarios = mutableMapOf<String, List<String>>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // Extract scenarios from providers
        resolver.getAllFiles().flatMap { it.declarations }
            .filterIsInstance<KSClassDeclaration>()
            .forEach { classDecl ->
                val isProvider = classDecl.superTypes.any {
                    it.resolve().declaration.qualifiedName?.asString() == "io.github.donald_okara.precover.core.PrecoverPreviewParameterProvider"
                }
                if (isProvider) {
                    val qualifiedName = classDecl.qualifiedName?.asString()
                    if (qualifiedName != null) {
                        providerScenarios[qualifiedName] = extractScenariosFromProvider(classDecl)
                        classDecl.containingFile?.let { sources.add(it) }
                    }
                }
            }

        val composables = resolver.getSymbolsWithAnnotation("androidx.compose.runtime.Composable")
            .filterIsInstance<KSFunctionDeclaration>()

        val currentMetadata = composables.map { extractMetadata(it) }.toList()
        metadataList.addAll(currentMetadata)

        composables.forEach { function ->
            function.containingFile?.let { file -> sources.add(file) }
            val functionName = function.simpleName.asString()
            val packageName = function.packageName.asString()
            val qualifiedName = "$packageName.$functionName"

            // Only infer calls for potential previews
            val isPotentialPreview = functionName.endsWith("Preview") ||
                functionName.contains("_") ||
                function.annotations.any {
                    val qName = it.annotationType.resolve().declaration.qualifiedName?.asString()
                    qName == "io.github.donald_okara.precover.core.annotations.PrecoverLink" ||
                        qName == "androidx.compose.ui.tooling.preview.Preview"
                }

            if (isPotentialPreview) {
                previewBodyCalls[qualifiedName] = inferCallsFromBody(function)
            }
        }

        return emptyList()
    }

    override fun finish() {
        if (metadataList.isEmpty()) return

        // 1. Identify "Base" functions (actual components)
        val baseFunctions = metadataList.filter {
            !it.functionName.endsWith("Preview") &&
                !it.functionName.contains("_") &&
                it.linkTargets.isEmpty()
        }

        // 2. Identify "Preview" functions (helpers/accessories)
        val previewFunctions = metadataList.filter {
            it.functionName.endsWith("Preview") ||
                it.functionName.contains("_") ||
                it.linkTargets.isNotEmpty()
        }.map { it.copy(isComponent = false) }

        val finalMetadata = baseFunctions.map { base ->
            val baseQualifiedName = "${base.packageName}.${base.functionName}"

            val expandedBasePreviews = expandPreviewsForMetadata(base)

            // Find any preview functions that should be attributed to this base function
            val attributedPreviews = previewFunctions
                .filter { previewFunc ->
                    val previewQualifiedName = "${previewFunc.packageName}.${previewFunc.functionName}"

                    // 1. Explicit Link (String name or Full Qualified Name)
                    val hasExplicitLink = previewFunc.linkTargets.any { target ->
                        target == baseQualifiedName ||
                            target == base.functionName ||
                            // 2. Type-Safe Link (NavKey or Marker Annotation)
                            base.parameters.any { it.type == target } ||
                            base.annotations.contains(target)
                    }

                    if (hasExplicitLink) return@filter true

                    // 3. Smart Body Analysis (Inferred Call)
                    val callsBase = previewBodyCalls[previewQualifiedName]?.contains(base.functionName) == true &&
                        previewFunc.packageName == base.packageName
                    if (callsBase) return@filter true

                    // 4. Naming Convention (fallback)
                    (
                        previewFunc.functionName == "${base.functionName}Preview" ||
                            previewFunc.functionName.startsWith("${base.functionName}_")
                        ) &&
                        previewFunc.packageName == base.packageName &&
                        previewFunc.fileName == base.fileName
                }
                .flatMap { previewFunc -> expandPreviewsForMetadata(previewFunc) }

            base.copy(previews = expandedBasePreviews + attributedPreviews)
        }

        if (finalMetadata.isEmpty()) return

        // 5. Include linked previews for linting/validation
        val linkedPreviews = previewFunctions.filter { it.linkTargets.isNotEmpty() }
        val allReportMetadata = finalMetadata + linkedPreviews

        val json = Json {
            prettyPrint = true
            encodeDefaults = true
        }
        val encoded = json.encodeToString(ListSerializer(ComposableMetadata.serializer()), allReportMetadata)

        try {
            val file = codeGenerator.createNewFile(
                dependencies = Dependencies(true, *sources.toTypedArray()),
                packageName = "io.github.donald_okara.precover",
                fileName = "precover-metadata",
                extensionName = "json",
            )
            file.write(encoded.toByteArray())
            file.close()
            logger.info("Precover: Metadata written to precover-metadata.json")
        } catch (e: Exception) {
            logger.error("Precover: Failed to write metadata: ${e.message}")
        }
    }

    private fun expandPreviewsForMetadata(metadata: ComposableMetadata): List<PreviewMetadata> {
        val scenarios = metadata.parameters
            .mapNotNull { param ->
                val limit = param.previewParameter?.limit
                val baseScenarios = param.previewParameter?.scenarios ?: emptyList()
                if (limit != null) baseScenarios.take(limit) else baseScenarios
            }
            .flatten()

        val basePreviews = metadata.previews

        return if (scenarios.isNotEmpty()) {
            if (basePreviews.isEmpty()) {
                scenarios.map { scenario ->
                    PreviewMetadata(name = scenario, scenario = scenario)
                }
            } else {
                basePreviews.flatMap { preview ->
                    scenarios.map { scenario ->
                        val combinedName = if (preview.name.isNullOrBlank()) scenario else "${preview.name} - $scenario"
                        preview.copy(name = combinedName, scenario = scenario)
                    }
                }
            }
        } else {
            // Semantic Naming Enhancement:
            // If the preview is named BaseName_State, extract "State" as the display name
            if (metadata.functionName.contains("_") && basePreviews.all { it.name.isNullOrBlank() }) {
                val state = normalizeScenarioName(metadata.functionName.substringAfter("_").removeSuffix("Preview"))
                basePreviews.map { it.copy(name = state, scenario = state) }
            } else {
                basePreviews
            }
        }
    }

    private fun normalizeScenarioName(name: String): String {
        val parts = if (name.any { it.isLowerCase() }) {
            name.split(Regex("(?=[A-Z])|_"))
        } else {
            name.split("_")
        }
        return parts.filter { it.isNotBlank() }
            .joinToString("") { part ->
                part.lowercase().replaceFirstChar { it.uppercase() }
            }
    }

    private fun extractScenariosFromProvider(classDecl: KSClassDeclaration): List<String> {
        val location = classDecl.location as? FileLocation ?: return emptyList()
        val file = File(location.filePath)
        if (!file.exists()) return emptyList()

        val lines = file.readLines()
        val startLine = location.lineNumber - 1
        if (startLine >= lines.size) return emptyList()

        // Extract class body
        val classBody = StringBuilder()
        var braceCount = 0
        var foundStart = false

        for (i in startLine until lines.size) {
            val line = lines[i]
            for (char in line) {
                if (char == '{') {
                    braceCount++
                    foundStart = true
                } else if (char == '}') {
                    braceCount--
                }
                if (foundStart) classBody.append(char)
                if (foundStart && braceCount == 0) break
            }
            if (foundStart && braceCount == 0) break
            classBody.append("\n")
        }

        val content = classBody.toString()
        // Regex to handle both literal strings and constants (e.g. PreviewScenario.Success)
        // Group 1: Literal string content
        // Group 2: Constant identifier (e.g. Success)
        val regex = Regex("""scenario\s*\(\s*(?:["']([^"']+)["']|(?:[A-Z][a-zA-Z0-9_]*\.)?([A-Z][a-zA-Z0-9_]*))\s*,""")
        val scenarios = regex.findAll(content).map { match ->
            val name = match.groupValues[1].takeIf { it.isNotBlank() } ?: match.groupValues[2]
            normalizeScenarioName(name)
        }.filter { it.isNotBlank() }.toList()

        if (scenarios.isNotEmpty()) {
            logger.info("Precover: Extracted ${scenarios.size} scenarios from ${classDecl.simpleName.asString()}")
        } else {
            logger.warn("Precover: No scenarios found in ${classDecl.qualifiedName?.asString()}. Use 'scenario(\"Name\", value)' to declare them.")
        }

        return scenarios
    }

    private fun inferCallsFromBody(function: KSFunctionDeclaration): Set<String> {
        val location = function.location as? FileLocation ?: return emptySet()
        val file = File(location.filePath)
        if (!file.exists()) return emptySet()

        val lines = file.readLines()
        val startLine = location.lineNumber - 1
        if (startLine >= lines.size) return emptySet()

        // Very basic body extraction: look for the first '{' and then match with '}'
        val bodyContent = StringBuilder()
        var braceCount = 0
        var foundStart = false

        for (i in startLine until lines.size) {
            val line = lines[i]
            for (char in line) {
                if (char == '{') {
                    braceCount++
                    foundStart = true
                } else if (char == '}') {
                    braceCount--
                }

                if (foundStart) {
                    bodyContent.append(char)
                }

                if (foundStart && braceCount == 0) {
                    break
                }
            }
            if (foundStart && braceCount == 0) break
            bodyContent.append("\n")
        }

        val body = bodyContent.toString()

        // Find potential composable calls (simple regex for PascalCase or camelCase followed by parenthesis)
        val regex = Regex("([A-Z][a-zA-Z0-9_]*|(?<![a-zA-Z0-9_])[a-z][a-zA-Z0-9_]*)\\s*\\(")
        return regex.findAll(body)
            .map { it.groupValues[1] }
            .filter { it != function.simpleName.asString() } // Avoid self-reference
            .toSet()
    }

    private fun extractMetadata(function: KSFunctionDeclaration): ComposableMetadata {
        val packageName = function.packageName.asString()
        val fileName = function.containingFile?.fileName ?: "Unknown"
        val functionName = function.simpleName.asString()
        val isInternal = function.modifiers.contains(Modifier.INTERNAL)

        val parameters = function.parameters.map { param ->
            val previewParam = param.annotations.find {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == "androidx.compose.ui.tooling.preview.PreviewParameter"
            }?.let { annotation ->
                val providerType = annotation.arguments.find { arg -> arg.name?.asString() == "provider" }?.value as? KSType
                val limit = annotation.arguments.find { arg -> arg.name?.asString() == "limit" }?.value as? Int
                val providerQualifiedName = providerType?.declaration?.qualifiedName?.asString() ?: "Unknown"

                PreviewParameterMetadata(
                    providerType = providerQualifiedName,
                    limit = limit,
                    scenarios = providerScenarios[providerQualifiedName] ?: emptyList(),
                )
            }

            ParameterMetadata(
                name = param.name?.asString() ?: "Unknown",
                type = param.type.resolve().declaration.qualifiedName?.asString() ?: "Unknown",
                hasDefaultValue = param.hasDefault,
                previewParameter = previewParam,
            )
        }

        val previews = extractPreviews(function)
        val linkTargets = extractLinkTargets(function)
        val requiredScenariosAnnotation = function.annotations.find {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == "io.github.donald_okara.precover.core.annotations.RequiresPreviewScenarios"
        }
        val requiredScenarios = requiredScenariosAnnotation?.let { annotation ->
            val values = annotation.arguments.find { it.name?.asString() == "values" }?.value as? List<*>
            values?.filterIsInstance<String>()?.map { normalizeScenarioName(it) }
        } ?: emptyList()

        val ignoreScenariosAnnotation = function.annotations.find {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == "io.github.donald_okara.precover.core.annotations.PrecoverIgnoreScenarios"
        }
        val ignoreScenarios = ignoreScenariosAnnotation?.let { annotation ->
            val values = annotation.arguments.find { it.name?.asString() == "exclude" }?.value as? List<*>
            values?.filterIsInstance<String>()?.map { normalizeScenarioName(it) }
        } ?: emptyList()
        val ignoreAllScenarios = ignoreScenariosAnnotation != null && ignoreScenarios.isEmpty()

        val noPreviewRequired = function.annotations.any {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == "io.github.donald_okara.precover.core.annotations.PrecoverNoPreviewRequired"
        }

        val annotations = function.annotations.mapNotNull { it.annotationType.resolve().declaration.qualifiedName?.asString() }.toList()

        return ComposableMetadata(
            packageName = packageName,
            fileName = fileName,
            functionName = functionName,
            isInternal = isInternal,
            parameters = parameters,
            previews = previews,
            linkTargets = linkTargets,
            requiredScenarios = requiredScenarios,
            ignoreScenarios = ignoreScenarios,
            ignoreAllScenarios = ignoreAllScenarios,
            noPreviewRequired = noPreviewRequired,
            annotations = annotations,
            hasDirectPreviews = previews.any { !it.isLink },
        )
    }

    private fun extractLinkTargets(annotated: KSAnnotated, visited: MutableSet<String> = mutableSetOf()): List<String> {
        val directLinks = annotated.annotations
            .filter { it.annotationType.resolve().declaration.qualifiedName?.asString() == "io.github.donald_okara.precover.core.annotations.PrecoverLink" }
            .mapNotNull { annotation ->
                val targetType = annotation.arguments.find { it.name?.asString() == "target" }?.value as? KSType
                val targetName = annotation.arguments.find { it.name?.asString() == "value" }?.value as? String

                if (!targetName.isNullOrBlank()) {
                    targetName
                } else if (targetType != null && targetType.declaration.qualifiedName?.asString() != "kotlin.Unit") {
                    targetType.declaration.qualifiedName?.asString()
                } else {
                    null
                }
            }
            .toList()

        val indirectLinks = annotated.annotations
            .filter {
                val qualifiedName = it.annotationType.resolve().declaration.qualifiedName?.asString()
                qualifiedName != null &&
                    qualifiedName != "androidx.compose.runtime.Composable" &&
                    qualifiedName != "androidx.compose.ui.tooling.preview.Preview" &&
                    qualifiedName != "io.github.donald_okara.precover.core.annotations.PrecoverLink" &&
                    !visited.contains(qualifiedName)
            }
            .flatMap { annotation ->
                val declaration = annotation.annotationType.resolve().declaration
                val qualifiedName = declaration.qualifiedName?.asString()
                if (qualifiedName != null && declaration is KSClassDeclaration && declaration.classKind == ClassKind.ANNOTATION_CLASS) {
                    visited.add(qualifiedName)
                    extractLinkTargets(declaration, visited)
                } else {
                    emptyList()
                }
            }
            .toList()

        return (directLinks + indirectLinks).distinct()
    }

    private fun extractPreviews(annotated: KSAnnotated, visited: MutableSet<String> = mutableSetOf()): List<PreviewMetadata> {
        val scenarioFromScenarioAnnotation = annotated.annotations.find {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == "io.github.donald_okara.precover.core.annotations.Scenario"
        }?.let { it.arguments.find { arg -> arg.name?.asString() == "value" }?.value as? String }

        val scenarioFromLinkAnnotation = annotated.annotations
            .filter { it.annotationType.resolve().declaration.qualifiedName?.asString() == "io.github.donald_okara.precover.core.annotations.PrecoverLink" }
            .mapNotNull { it.arguments.find { arg -> arg.name?.asString() == "scenario" }?.value as? String }
            .firstOrNull { it.isNotBlank() }

        val scenario = scenarioFromScenarioAnnotation ?: scenarioFromLinkAnnotation
        val normalizedScenario = scenario?.let { normalizeScenarioName(it) }

        val directPreviews = annotated.annotations
            .filter {
                val name = it.annotationType.resolve().declaration.qualifiedName?.asString()
                name == "androidx.compose.ui.tooling.preview.Preview"
            }
            .map { parsePreviewAnnotation(it).copy(scenario = normalizedScenario) }

        val indirectPreviews = annotated.annotations
            .filter {
                val qualifiedName = it.annotationType.resolve().declaration.qualifiedName?.asString()
                qualifiedName != null &&
                    qualifiedName != "androidx.compose.runtime.Composable" &&
                    qualifiedName != "androidx.compose.ui.tooling.preview.Preview" &&
                    qualifiedName != "io.github.donald_okara.precover.core.annotations.PrecoverLink" &&
                    !visited.contains(qualifiedName)
            }
            .flatMap { annotation ->
                val declaration = annotation.annotationType.resolve().declaration
                val qualifiedName = declaration.qualifiedName?.asString()
                if (qualifiedName != null && declaration is KSClassDeclaration && declaration.classKind == ClassKind.ANNOTATION_CLASS) {
                    visited.add(qualifiedName)
                    extractPreviews(declaration, visited)
                } else {
                    emptyList()
                }
            }
            .map { preview ->
                if (preview.scenario == null && normalizedScenario != null) {
                    preview.copy(scenario = normalizedScenario)
                } else {
                    preview
                }
            }

        return directPreviews.toList() + indirectPreviews.toList()
    }

    private fun parsePreviewAnnotation(annotation: KSAnnotation): PreviewMetadata {
        val args = annotation.arguments.associate { it.name?.asString() to it.value }

        return PreviewMetadata(
            name = args["name"] as? String,
            group = args["group"] as? String,
            apiLevel = args["apiLevel"] as? Int,
            widthDp = args["widthDp"] as? Int,
            heightDp = args["heightDp"] as? Int,
            locale = args["locale"] as? String,
            fontScale = args["fontScale"] as? Float,
            showBackground = args["showBackground"] as? Boolean,
            backgroundColor = (args["backgroundColor"] as? Number)?.toLong(),
            showSystemUi = args["showSystemUi"] as? Boolean,
            device = args["device"] as? String,
            uiMode = args["uiMode"] as? Int,
            wallpaper = args["wallpaper"] as? Int,
            isLink = false,
        )
    }
}

class PrecoverProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = PrecoverProcessor(environment.codeGenerator, environment.logger, environment.options)
}
