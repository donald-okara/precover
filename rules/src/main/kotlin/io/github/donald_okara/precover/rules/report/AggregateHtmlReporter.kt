package io.github.donald_okara.precover.rules.report

import io.github.donald_okara.precover.core.models.AggregateCoverageReport

class AggregateHtmlReporter {
    fun generate(report: AggregateCoverageReport): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Precover Aggregate Coverage Report</title>
                <style>
                    body { font-family: sans-serif; margin: 20px; background-color: #f4f4f9; }
                    h1 { color: #333; }
                    .summary { background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 20px; }
                    .score { font-size: 48px; font-weight: bold; color: ${if (report.overallScore > 80) "#4CAF50" else if (report.overallScore > 50) "#FF9800" else "#f44336"}; }
                    .module { background: #fff; padding: 15px; border-radius: 8px; margin-bottom: 10px; border-left: 5px solid #ccc; display: flex; justify-content: space-between; align-items: center; }
                    .module.good { border-left-color: #4CAF50; }
                    .module.warn { border-left-color: #FF9800; }
                    .module.bad { border-left-color: #f44336; }
                    .module-info { flex-grow: 1; }
                    .module-score { font-size: 24px; font-weight: bold; }
                    .link { margin-left: 20px; }
                </style>
            </head>
            <body>
                <h1>Precover Aggregate Coverage Report</h1>
                <div class="summary">
                    <div>Project Overall Coverage Score</div>
                    <div class="score">${"%.1f".format(report.overallScore)}%</div>
                </div>
                
                <h2>Module Breakdown</h2>
                ${report.modules.joinToString("") { module ->
                    val statusClass = if (module.score > 80) "good" else if (module.score > 50) "warn" else "bad"
                    val reportPath = module.reportPath
                    """
                    <div class="module $statusClass">
                        <div class="module-info">
                            <h3>${escapeHtml(module.modulePath)}</h3>
                            <p>${module.componentCount} components analyzed</p>
                        </div>
                        <div class="module-score">${"%.1f".format(module.score)}%</div>
                        ${if (reportPath != null) """<div class="link"><a href="${escapeHtml(reportPath)}">View Details</a></div>""" else ""}
                    </div>
                    """
                }}
            </body>
            </html>
        """.trimIndent()
    }

    private fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
}
