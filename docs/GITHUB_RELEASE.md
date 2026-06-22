# GitHub Push and Release Guide

This guide walks through pushing the project to GitHub and creating the first versioned release.

---

## Prerequisites

- A GitHub account
- `git` installed locally
- `gh` (GitHub CLI) installed — optional but recommended for creating releases

---

## Step 1: Create a GitHub repository

1. Go to https://github.com/new
2. Enter repository name: `api-key-manager`
3. Choose **Private** or **Public**
4. Do NOT initialise with a README, .gitignore, or license — the project already has these
5. Click **Create repository**

---

## Step 2: Initialise git and push

Run these commands from the `api-key-manager/` project root:

```bash
# Initialise a git repository if you haven't already
git init

# Stage everything (the .gitignore excludes secrets, build outputs, etc.)
git add .

# Verify no secrets are staged — you should NOT see .env in this list
git status

# Create the initial commit
git commit -m "chore: initial release v0.1.0"

# Point to your new GitHub repo (replace <your-username>)
git remote add origin https://github.com/<your-username>/api-key-manager.git

# Push
git branch -M main
git push -u origin main
```

---

## Step 3: Confirm no secrets were committed

```bash
# Search the entire git history for common secret patterns
git log --all --oneline --source --remotes -- '*.env'
git grep -r "POSTGRES_PASSWORD\|JWT_SECRET\|ADMIN_PASSWORD" -- ':!*.example' ':!*.md'
```

If either command returns results from non-example files, remove the file, add it to `.gitignore`, and rewrite history with `git filter-repo` before pushing.

---

## Step 4: Create a GitHub release

### Option A — with GitHub CLI (recommended)

```bash
# Tag the current commit
git tag -a v0.1.0 -m "Release v0.1.0"
git push origin v0.1.0

# Create the release with release notes
gh release create v0.1.0 \
  --title "v0.1.0 — Initial release" \
  --notes "$(cat CHANGELOG.md)"
```

### Option B — via the GitHub web UI

1. Go to your repository on GitHub
2. Click **Releases** → **Create a new release**
3. Click **Choose a tag** and type `v0.1.0`, then click **Create new tag: v0.1.0 on publish**
4. Set the release title to `v0.1.0 — Initial release`
5. Paste the contents of `CHANGELOG.md` into the description
6. Click **Publish release**

---

## Step 5: Verify CI passes

After pushing, go to the **Actions** tab on GitHub and confirm the CI workflow (`ci.yml`) passes all checks:

- Backend: `./mvnw verify`
- Frontend: `npm run build` + `npm test`

If CI is red, fix the issue, commit, and push to the same branch.

---

## Cutting future releases

1. Update `CHANGELOG.md` with the changes since the last release
2. Bump the version in `backend/pom.xml` (`<version>`) and `frontend/package.json` (`"version"`)
3. Commit: `git commit -m "chore: bump version to vX.Y.Z"`
4. Tag and release:
   ```bash
   git tag -a vX.Y.Z -m "Release vX.Y.Z"
   git push origin main --tags
   gh release create vX.Y.Z --title "vX.Y.Z" --notes "..."
   ```

---

## Branch protection (recommended)

In **Settings → Branches → Add rule** on GitHub:

- Branch name pattern: `main`
- Enable **Require a pull request before merging**
- Enable **Require status checks to pass before merging** and select the CI workflow
- Enable **Do not allow bypassing the above settings**
