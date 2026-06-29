package io.github.donald_okara.precover.rules.report

import io.github.donald_okara.precover.core.models.CoverageReport
import io.github.donald_okara.precover.core.models.Severity

class HtmlReporter {
    fun generate(report: CoverageReport): String = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Precover Coverage Report</title>
                <style>
                    body { font-family: sans-serif; margin: 20px; background-color: #f4f4f9; }
                    h1 { color: #333; }
                    .summary { background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 20px; }
                    .score { font-size: 48px; font-weight: bold; color: ${if (report.overallScore > 80) {
        "#4CAF50"
    } else if (report.overallScore > 50) {
        "#FF9800"
    } else {
        "#f44336"
    }}; }
                    .component { background: #fff; padding: 15px; border-radius: 8px; margin-bottom: 10px; border-left: 5px solid #ccc; }
                    .component.good { border-left-color: #4CAF50; }
                    .component.warn { border-left-color: #FF9800; }
                    .component.bad { border-left-color: #f44336; }
                    .violation { margin-left: 20px; font-size: 14px; }
                    .ERROR { color: #f44336; font-weight: bold; }
                    .WARNING { color: #FF9800; }
                    .INFO { color: #2196F3; }
                </style>
            </head>
            <body>
                <h1>Precover Coverage Report</h1>
                <div class="summary">
                    <div>Overall Coverage Score</div>
                    <div class="score">${"%.1f".format(report.overallScore)}%</div>
                </div>
                
                <h2>Component Details</h2>
                ${report.components.filter { it.isComponent }.joinToString("") { component ->
        val statusClass = if (component.score > 80) {
            "good"
        } else if (component.score > 50) {
            "warn"
        } else {
            "bad"
        }
        """
                    <div class="component $statusClass">
                        <h3>${escapeHtml(component.name)} <small>(${escapeHtml(component.packageName)})</small> - ${"%.1f".format(component.score)}%</h3>
                        ${if (component.violations.isEmpty()) {
            "<p>No violations found.</p>"
        } else {
            component.violations.joinToString("") {
                """<div class="violation"><span class="${it.severity}">${it.severity}:</span> ${escapeHtml(it.message)}</div>"""
            }
        }}
                    </div>
                    """
    }}

                ${if (report.components.any { !it.isComponent }) "<h2>Preview Accessory Validation</h2>" else ""}
                ${report.components.filter { !it.isComponent }.joinToString("") { component ->
        val statusClass = if (component.score >= 100) "good" else "bad"
        """
                    <div class="component $statusClass">
                        <h3>${escapeHtml(component.name)} <small>(${escapeHtml(component.packageName)})</small> - ${if (component.score >= 100) "Valid" else "Invalid"}</h3>
                        ${if (component.violations.isEmpty()) {
            "<p>No configuration issues found.</p>"
        } else {
            component.violations.joinToString("") {
                """<div class="violation"><span class="${it.severity}">${it.severity}:</span> ${escapeHtml(it.message)}</div>"""
            }
        }}
                    </div>
                    """
    }}
            </body>
            </html>
    """.trimIndent()

    private fun escapeHtml(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")
}
