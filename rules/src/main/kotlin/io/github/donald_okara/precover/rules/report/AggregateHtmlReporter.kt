package io.github.donald_okara.precover.rules.report

import io.github.donald_okara.precover.core.models.AggregateCoverageReport

class AggregateHtmlReporter {
    fun generate(report: AggregateCoverageReport): String = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Precover Aggregate Report</title>
                <style>
                    :root {
                        --bg-color: #f8f9fa;
                        --card-bg: #ffffff;
                        --text-main: #212529;
                        --text-muted: #6c757d;
                        --success: #28a745;
                        --warning: #ffc107;
                        --danger: #dc3545;
                        --primary: #6200ee;
                        --border-color: #dee2e6;
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
                    
                    .section-header { margin-bottom: 20px; padding-bottom: 10px; border-bottom: 2px solid var(--border-color); }
                    .section-header h2 { margin: 0; font-size: 22px; }
                    
                    .module-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(350px, 1fr)); gap: 20px; }
                    .module-card { background: var(--card-bg); padding: 25px; border-radius: 12px; box-shadow: 0 2px 4px rgba(0,0,0,0.03); border: 1px solid var(--border-color); display: flex; flex-direction: column; transition: transform 0.2s ease, box-shadow 0.2s ease; }
                    .module-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
                    
                    .module-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 15px; }
                    .module-title h3 { margin: 0; font-size: 18px; color: var(--primary); }
                    .module-title p { margin: 5px 0 0; color: var(--text-muted); font-size: 14px; }
                    
                    .module-score { font-size: 32px; font-weight: 800; color: ${"#444"}; }
                    .module-card.good .module-score { color: var(--success); }
                    .module-card.warn .module-score { color: var(--warning); }
                    .module-card.bad .module-score { color: var(--danger); }
                    
                    .module-footer { margin-top: auto; padding-top: 20px; border-top: 1px solid var(--border-color); display: flex; justify-content: flex-end; }
                    .btn { display: inline-flex; align-items: center; gap: 8px; padding: 8px 16px; border-radius: 6px; font-size: 14px; font-weight: 600; text-decoration: none; transition: all 0.2s; }
                    .btn-primary { background: var(--primary); color: white; }
                    .btn-primary:hover { opacity: 0.9; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="summary">
                        <div class="summary-info">
                            <h1>Project Coverage Overview</h1>
                            <p>Aggregate Analysis of all sub-projects</p>
                        </div>
                        <div class="score-container">
                            <div class="score-label">Global Score</div>
                            <div class="score">${"%.1f".format(report.overallScore)}%</div>
                        </div>
                    </div>
                </div>
                
                <div class="section-header">
                    <h2>Module Breakdown</h2>
                </div>
                
                <div class="module-grid">
                    ${report.modules.joinToString("") { module ->
        val statusClass = if (module.score > 80) {
            "good"
        } else if (module.score > 50) {
            "warn"
        } else {
            "bad"
        }
        val reportPath = module.reportPath
        """
                    <div class="module-card $statusClass">
                        <div class="module-header">
                            <div class="module-title">
                                <h3>${escapeHtml(module.modulePath)}</h3>
                                <p>${module.componentCount} components analyzed</p>
                            </div>
                            <div class="module-score">${"%.1f".format(module.score)}%</div>
                        </div>
                        
                        ${if (reportPath != null) {
            """
                        <div class="module-footer">
                            <a href="${escapeHtml(reportPath)}" class="btn btn-primary">
                                View Details
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path><polyline points="15 3 21 3 21 9"></polyline><line x1="10" y1="14" x2="21" y2="3"></line></svg>
                            </a>
                        </div>
                        """
        } else {
            ""
        }}
                    </div>
                    """
    }}
                </div>
            </body>
            </html>
    """.trimIndent()

    private fun getScoreColor(score: Float): String = when {
        score > 80 -> "var(--success)"
        score > 50 -> "var(--warning)"
        else -> "var(--danger)"
    }

    private fun escapeHtml(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")
}
