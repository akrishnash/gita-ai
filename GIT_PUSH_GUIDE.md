# How to Push to Git

## Step 1: Create a Remote Repository

1. Go to GitHub (https://github.com), GitLab, or Bitbucket
2. Create a new repository (don't initialize with README)
3. Copy the repository URL (e.g., `https://github.com/username/repo-name.git`)

## Step 2: Add Remote and Push

### If you have a repository URL:

```bash
# Add the remote repository
git remote add origin <your-repository-url>

# Example:
# git remote add origin https://github.com/yourusername/gita-android-app.git

# Push your code
git push -u origin master
```

### If your default branch is 'main' instead of 'master':

```bash
# First, rename your branch if needed
git branch -M main

# Then push
git push -u origin main
```

## Step 3: Verify

After pushing, you can verify with:
```bash
git remote -v
git log --oneline
```

## Common Issues

### If you get "repository not found":
- Check that the repository URL is correct
- Make sure you have access to the repository
- Verify your Git credentials are set up

### If you get authentication errors:
- For HTTPS: You may need a Personal Access Token instead of password
- For SSH: Make sure your SSH key is added to your Git provider

### If the remote already exists:
```bash
# Remove existing remote
git remote remove origin

# Add new remote
git remote add origin <your-repository-url>

# Push
git push -u origin master
```

