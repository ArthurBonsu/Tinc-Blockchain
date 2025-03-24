# Cleaning the Repository and Pushing Large Files to GitHub

This guide explains how to clean up a Git repository by removing large files from history and pushing the cleaned repository to GitHub.

## Step 1: Expire Reflog Entries

To ensure that any large file references in Git history are expired, run:

```bash
git reflog expire --expire=now --all
This removes any saved references to previous commits that might still contain large files.

Step 2: Perform Garbage Collection
To clean up unnecessary files and optimize the repository, execute:

bash
Copy
Edit
git gc --prune=now --aggressive
This command aggressively removes unnecessary files and compresses the repository.

Step 3: Force Push Cleaned Repository to GitHub
After cleaning up the repository, push the changes forcefully to override previous commits:

bash
Copy
Edit
git push --force origin main
This ensures that the large files are completely removed from GitHub.

Step 4: Verify Cleanup on GitHub
Once the push is complete, check your GitHub repository to ensure that the large files no longer exist in the history.

