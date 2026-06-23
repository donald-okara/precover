package io.github.donald_okara.precover.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import io.github.donald_okara.precover.core.models.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer

class PrecoverProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    private val metadataList = mutableListOf<ComposableMetadata>()
    private val sources = mutableListOf<KSFile>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val composables = resolver.getSymbolsWithAnnotation("androidx.compose.runtime.Composable")
            .filterIsInstance<KSFunctionDeclaration>()

        val currentMetadata = composables.map { extractMetadata(it) }.toList()
        metadataList.addAll(currentMetadata)
        
        composables.forEach { it.containingFile?.let { file -> sources.add(file) } }

        return emptyList()
    }

    override fun finish() {
        if (metadataList.isEmpty()) return

        val json = Json { prettyPrint = true }
        val encoded = json.encodeToString(ListSerializer(ComposableMetadata.serializer()), metadataList)

        try {
            val file = codeGenerator.createNewFile(
                dependencies = Dependencies(true, *sources.toTypedArray()),
                packageName = "io.github.donald_okara.precover",
                fileName = "precover-metadata",
                extensionName = "json"
            )
            file.write(encoded.toByteArray())
            file.close()
            logger.info("Precover: Metadata written to precover-metadata.json")
        } catch (e: Exception) {
            logger.error("Precover: Failed to write metadata: ${e.message}")
        }
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
                PreviewParameterMetadata(
                    providerType = providerType?.declaration?.qualifiedName?.asString() ?: "Unknown",
                    limit = limit
                )
            }

            ParameterMetadata(
                name = param.name?.asString() ?: "Unknown",
                type = param.type.resolve().declaration.qualifiedName?.asString() ?: "Unknown",
                hasDefaultValue = param.hasDefault,
                previewParameter = previewParam
            )
        }

        val previews = extractPreviews(function)

        return ComposableMetadata(
            packageName = packageName,
            fileName = fileName,
            functionName = functionName,
            isInternal = isInternal,
            parameters = parameters,
            previews = previews
        )
    }

    private fun extractPreviews(annotated: KSAnnotated, visited: MutableSet<String> = mutableSetOf()): List<PreviewMetadata> {
        val directPreviews = annotated.annotations
            .filter { it.annotationType.resolve().declaration.qualifiedName?.asString() == "androidx.compose.ui.tooling.preview.Preview" }
            .map { parsePreviewAnnotation(it) }

        val indirectPreviews = annotated.annotations
            .filter { 
                val qualifiedName = it.annotationType.resolve().declaration.qualifiedName?.asString()
                qualifiedName != null &&
                qualifiedName != "androidx.compose.runtime.Composable" && 
                qualifiedName != "androidx.compose.ui.tooling.preview.Preview" &&
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
            wallpaper = args["wallpaper"] as? Int
        )
    }
}

class PrecoverProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return PrecoverProcessor(environment.codeGenerator, environment.logger, environment.options)
    }
}
