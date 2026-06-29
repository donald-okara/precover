# Issue Skill

This skill allows the agent to check GitHub issues and gather context for tasks.

## Workflow

1. **Identify the Issue**:
   - If the task is related to a specific issue number mentioned by the user, use that.
   - If the task is associated with the current branch and the branch name contains an issue number (e.g., `fix/123-bug-fix`), assume that is the issue.
   - **If no issue is clearly identified**:
     - Use `gh issue list` to show recent issues and ask the user for the issue number.
     - Or ask the user directly for the issue number or URL.

2. **Fetch Issue Details**: Use the `gh` CLI to fetch the issue's description and comments.
   - Example command to view issue details:
     `gh issue view {ISSUE_NUMBER}`
   - Example command to list issue comments:
     `gh issue view {ISSUE_NUMBER} --json comments`

3. **Analyze and Plan**:
   - Read the issue description carefully to understand the requirements or the reported bug.
   - Review comments for additional context, reproduction steps, or suggested solutions.
   - Formulate a plan to address the issue.

4. **Implementation**:
   - If the task involves fixing a bug or implementing a feature described in the issue, follow the standard development workflow.
   - If a suggestion in the issue or its comments is unclear, **always ask the user for clarification**.

5. **Update and Report**:
   - Summarize findings or progress based on the issue details.
   - If the task is completed, mention that it addresses the specific issue.

## Implementation Principles

- **Context First**: Always ensure you have a full understanding of the issue before making changes.
- **Engagement**: Ask questions whenever there is ambiguity in the issue description or comments.
- **Traceability**: Reference the issue number in your progress updates and final report.
