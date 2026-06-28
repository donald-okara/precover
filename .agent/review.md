# Review Skill

This skill allows the agent to pull reviews from GitHub and selectively implement the suggested changes.

## Workflow

1. **Identify the Pull Request**: 
   - Always assume the Pull Request in question is the one attached to the **current branch**.
   - Use `gh pr view --json number,url,baseRefName` to identify the PR for the current branch.
   - **If no PR is found for the current branch**:
     - Ask the user which branch or PR should be used.
     - Once the user provides a branch, check it out using `git checkout {BRANCH_NAME}` and then proceed.
2. **Fetch Reviews**: Use the `gh` CLI to fetch comments and reviews for the identified PR.
   - Example command to list PR reviews:
     `gh pr view {PR_NUMBER} --json reviews`
   - Example command to list PR comments (including line-specific suggestions):
     `gh pr view {PR_NUMBER} --json comments`
3. **Analyze and Filter**: Review the fetched comments.
   - Ignore resolved comments or those that are purely conversational.
   - Focus on actionable suggestions and requested changes.
4. **Selective Implementation**:
   - For each actionable item, evaluate if it is necessary or beneficial for the project.
   - If a suggestion is unclear or you are unsure about its impact, **always ask the user for clarification**.
5. **Apply Changes**: Implement the selected changes using standard editing tools.
6. **Report**: Summarize the changes implemented and any suggestions that were skipped or require further discussion.

## Implementation Principles

- **Prudence**: Only implement changes that align with the project's goals and code style.
- **Transparency**: Clearly communicate what has been implemented and what hasn't.
- **Engagement**: Ask questions whenever there is ambiguity.
