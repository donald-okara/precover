package io.github.donald_okara.precover.rules.report

import io.github.donald_okara.precover.core.models.CoverageReport
import io.github.donald_okara.precover.core.models.Severity

class HtmlReporter {
    fun generate(report: CoverageReport): String = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Precover Coverage Report</title>
                <style>
                    :root {
                        --bg-color: #f8f9fa;
                        --card-bg: #ffffff;
                        --text-main: #212529;
                        --text-muted: #6c757d;
                        --success: #28a745;
                        --warning: #ffc107;
                        --danger: #dc3545;
                        --info: #17a2b8;
                        --border-color: #dee2e6;
                        --primary: #6200ee;
                    }
                    body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; margin: 0; padding: 40px; background-color: var(--bg-color); color: var(--text-main); line-height: 1.5; }
                    h1, h2, h3 { color: #1a1a1a; margin-top: 0; }
                    .header { margin-bottom: 40px; }
                    .summary { background: var(--card-bg); padding: 30px; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); margin-bottom: 30px; display: flex; align-items: center; justify-content: space-between; border: 1px solid var(--border-color); }
                    .summary-info h1 { margin-bottom: 5px; font-size: 28px; }
                    .summary-info p { margin: 0; color: var(--text-muted); font-size: 16px; }
                    .score-container { text-align: right; }
                    .score-label { font-size: 14px; text-transform: uppercase; letter-spacing: 1px; color: var(--text-muted); font-weight: bold; margin-bottom: 5px; }
                    .score { font-size: 56px; font-weight: 800; line-height: 1; color: ${getScoreColor(report.overallScore)}; }
                    
                    .section-header { display: flex; align-items: center; margin-bottom: 20px; padding-bottom: 10px; border-bottom: 2px solid var(--border-color); }
                    .section-header h2 { margin: 0; font-size: 22px; flex-grow: 1; }
                    
                    .component { background: var(--card-bg); padding: 25px; border-radius: 12px; margin-bottom: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.03); border: 1px solid var(--border-color); position: relative; overflow: hidden; }
                    .component::before { content: ''; position: absolute; left: 0; top: 0; bottom: 0; width: 6px; background-color: ${"#ccc"}; }
                    .component.good::before { background-color: var(--success); }
                    .component.warn::before { background-color: var(--warning); }
                    .component.bad::before { background-color: var(--danger); }
                    
                    .component-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; }
                    .component-title h3 { margin: 0; font-size: 20px; display: flex; align-items: center; gap: 10px; }
                    .component-title small { font-weight: normal; color: var(--text-muted); font-size: 14px; }
                    
                    .component-score { font-size: 24px; font-weight: bold; color: ${"#444"}; }
                    .component.good .component-score { color: var(--success); }
                    .component.warn .component-score { color: #d39e00; }
                    .component.bad .component-score { color: var(--danger); }

                    .badge { display: inline-flex; align-items: center; padding: 4px 10px; border-radius: 20px; font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; }
                    .badge-scenario { background: #e3f2fd; color: #0d47a1; border: 1px solid #bbdefb; }
                    .badge-accessory { background: #f3e5f5; color: #4a148c; border: 1px solid #e1bee7; }

                    .scenario-section { background: #f8f9fa; padding: 15px; border-radius: 8px; margin-bottom: 20px; border: 1px solid #edf2f7; }
                    .section-title { font-size: 13px; font-weight: 700; color: #4a5568; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 12px; display: flex; align-items: center; justify-content: space-between; }
                    
                    .progress-bar-container { height: 8px; background: #e2e8f0; border-radius: 4px; margin-bottom: 12px; overflow: hidden; }
                    .progress-bar { height: 100%; background: var(--success); border-radius: 4px; transition: width 0.3s ease; }
                    
                    .scenario-grid { display: flex; flex-wrap: wrap; gap: 8px; }
                    .scenario-tag { padding: 4px 12px; border-radius: 6px; font-size: 12px; font-weight: 500; display: flex; align-items: center; gap: 6px; border: 1px solid transparent; }
                    .scenario-tag.covered { background: #ecfdf5; color: #065f46; border-color: #a7f3d0; }
                    .scenario-tag.missing { background: #fef2f2; color: #991b1b; border-color: #fecaca; }
                    .scenario-tag.info { background: #f0f9ff; color: #075985; border-color: #bae6fd; }
                    .scenario-tag svg { width: 14px; height: 14px; }

                    .rules-section { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 10px; }
                    .rule-item { display: flex; align-items: center; gap: 10px; font-size: 14px; padding: 8px 12px; border-radius: 6px; background: #fff; border: 1px solid #edf2f7; }
                    .rule-item.pass { color: var(--success); }
                    .rule-item.violation { grid-column: 1 / -1; align-items: flex-start; }
                    .rule-status-icon { flex-shrink: 0; display: flex; align-items: center; justify-content: center; }
                    
                    .violation-content { display: flex; flex-direction: column; }
                    .violation-msg { color: var(--text-main); }
                    .violation-rule { font-size: 11px; font-weight: bold; text-transform: uppercase; margin-bottom: 2px; }
                    .ERROR { color: var(--danger); }
                    .WARNING { color: var(--warning); }
                    .INFO { color: var(--info); }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="summary">
                        <div class="summary-info">
                            <h1>Precover Coverage Report</h1>
                            <p>Generated for module <strong>${escapeHtml(report.modulePath ?: "root")}</strong></p>
                        </div>
                        <div class="score-container">
                            <div class="score-label">Overall Coverage</div>
                            <div class="score">${"%.1f".format(report.overallScore)}%</div>
                        </div>
                    </div>
                </div>
                
                <div class="section-header">
                    <h2>Component Coverage</h2>
                    <span class="badge badge-scenario">${report.components.count { it.isComponent }} Components</span>
                </div>

                ${report.components.filter { it.isComponent }.joinToString("") { component ->
        val statusClass = if (component.score > 80) {
            "good"
        } else if (component.score > 50) {
            "warn"
        } else {
            "bad"
        }
        val hasScenarios = component.requiredScenarios.isNotEmpty() || component.coveredScenarios.isNotEmpty()
        val coveredScenariosSet = component.coveredScenarios.toSet()

        """
                    <div class="component $statusClass">
                        <div class="component-header">
                            <div class="component-title">
                                <h3>
                                    ${escapeHtml(component.name)}
                                    ${if (hasScenarios) """<span class="badge badge-scenario">Scenarios</span>""" else ""}
                                </h3>
                                <small>${escapeHtml(component.packageName)}</small>
                            </div>
                            <div class="component-score">${"%.1f".format(component.score)}%</div>
                        </div>
                        
                        ${if (component.requiredScenarios.isNotEmpty()) {
            val coveredCount = component.requiredScenarios.count { it in coveredScenariosSet }
            val coveragePercent = (coveredCount.toFloat() / component.requiredScenarios.size.toFloat()) * 100f
            """
                            <div class="scenario-section">
                                <div class="section-title">
                                    <span>Scenario Coverage</span>
                                    <span>$coveredCount / ${component.requiredScenarios.size}</span>
                                </div>
                                <div class="progress-bar-container">
                                    <div class="progress-bar" style="width: $coveragePercent%"></div>
                                </div>
                                <div class="scenario-grid">
                                    ${component.requiredScenarios.joinToString("") { scenario ->
                val isCovered = scenario in coveredScenariosSet
                val stateClass = if (isCovered) "covered" else "missing"
                val icon = if (isCovered) checkIcon() else xIcon()
                """<div class="scenario-tag $stateClass">$icon ${escapeHtml(formatScenarioName(scenario))}</div>"""
            }}
                                </div>
                            </div>
                            """
        } else if (component.coveredScenarios.isNotEmpty()) {
            """
                            <div class="scenario-section">
                                <div class="section-title">Verified Scenarios</div>
                                <div class="scenario-grid">
                                    ${component.coveredScenarios.distinct().joinToString("") { scenario ->
                """<div class="scenario-tag info">${infoIcon()} ${escapeHtml(formatScenarioName(scenario))}</div>"""
            }}
                                </div>
                            </div>
                            """
        } else {
            ""
        }}

                        <div class="section-title">Rule Validation</div>
                        <div class="rules-section">
                            ${component.violations.joinToString("") {
            """
                                <div class="rule-item violation">
                                    <div class="rule-status-icon">${alertIcon(it.severity.name)}</div>
                                    <div class="violation-content">
                                        <div class="violation-rule ${it.severity}">${it.ruleName}</div>
                                        <div class="violation-msg">${escapeHtml(it.message)}</div>
                                    </div>
                                </div>
                                """
        }}
                            ${component.passedRules.joinToString("") {
            """
                                <div class="rule-item pass">
                                    <div class="rule-status-icon">${checkIcon("var(--success)")}</div>
                                    <span>${escapeHtml(it)}</span>
                                </div>
                                """
        }}
                        </div>
                    </div>
                    """
    }}

                ${if (report.components.any { !it.isComponent }) {
        """
                <div class="section-header" style="margin-top: 40px;">
                    <h2>Preview Accessories</h2>
                    <span class="badge badge-accessory">${report.components.count { !it.isComponent }} Items</span>
                </div>
                """
    } else {
        ""
    }}
                
                ${report.components.filter { !it.isComponent }.joinToString("") { component ->
        val statusClass = if (component.score >= 100) "good" else "bad"
        """
                    <div class="component $statusClass">
                        <div class="component-header">
                            <div class="component-title">
                                <h3>
                                    ${escapeHtml(component.name)}
                                    <span class="badge badge-accessory">Accessory</span>
                                </h3>
                                <small>${escapeHtml(component.packageName)}</small>
                            </div>
                            <div class="component-score">${if (component.score >= 100) "Valid" else "Invalid"}</div>
                        </div>
                        
                        <div class="section-title">Validation Details</div>
                        <div class="rules-section">
                            ${component.violations.joinToString("") {
            """
                                <div class="rule-item violation">
                                    <div class="rule-status-icon">${alertIcon(it.severity.name)}</div>
                                    <div class="violation-content">
                                        <div class="violation-rule ${it.severity}">${it.ruleName}</div>
                                        <div class="violation-msg">${escapeHtml(it.message)}</div>
                                    </div>
                                </div>
                                """
        }}
                            ${component.passedRules.joinToString("") {
            """
                                <div class="rule-item pass">
                                    <div class="rule-status-icon">${checkIcon("var(--success)")}</div>
                                    <span>${escapeHtml(it)}</span>
                                </div>
                                """
        }}
                        </div>
                    </div>
                    """
    }}
            </body>
            </html>
    """.trimIndent()

    private fun getScoreColor(score: Float): String = when {
        score > 80 -> "var(--success)"
        score > 50 -> "var(--warning)"
        else -> "var(--danger)"
    }

    private fun checkIcon(color: String = "currentColor") = """<svg fill="none" stroke="$color" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"></path></svg>"""
    private fun xIcon() = """<svg fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M6 18L18 6M6 6l12 12"></path></svg>"""
    private fun infoIcon() = """<svg fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>"""
    private fun alertIcon(severity: String) = when (severity) {
        "ERROR" -> """<svg fill="none" stroke="var(--danger)" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>"""
        "WARNING" -> """<svg fill="none" stroke="var(--warning)" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>"""
        else -> infoIcon()
    }

    private fun escapeHtml(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")

    private fun formatScenarioName(name: String): String {
        val parts = if (name.any { it.isLowerCase() }) {
            name.split(Regex("(?=[A-Z])|_"))
        } else {
            name.split("_")
        }
        return parts.filter { it.isNotBlank() }
            .joinToString(" ") { part ->
                part.lowercase().replaceFirstChar { it.uppercase() }
            }
    }
}
