# Git History Cleanup Guide (Optional)

This guide documents how to permanently purge the historical blobs (Perfetto traces and QA screenshots) from Git history **if you choose to rewrite history in the future**.

---

## 1. Why is `.git/` Still ~36.6 MB?

When you delete large files using `git rm`, Git records a new commit where those files no longer appear in the working tree. However, **Git is an append-only version control system**. The historical commits (such as when the traces and screenshots were originally committed) still retain pointers to those compressed objects (blobs) in `.git/objects/pack/`.

This ensures that checking out previous commits reproduces the exact repository state at that time.

---

## 2. Should You Rewrite History?

> [!WARNING]
> Rewriting Git history modifies commit hashes and requires force-pushing. It should only be done if you are comfortable with the consequences below.

### Consequences of Rewriting History
1. **Commit Hashes Change**: Every commit after the earliest modified commit will receive a new SHA-1 hash.
2. **Collaborator Disruptions**: Anyone with an existing clone will have diverging histories and must re-clone or hard-reset (`git reset --hard origin/main`).
3. **Open Pull Requests**: Any open branches or PRs will become disconnected from `main` and must be rebased.
4. **Force Push Required**: You will need to run `git push origin --force --all --tags`.

---

## 3. Recommended Procedure: `git-filter-repo`

If you decide to purge the large blobs, the official and fastest tool recommended by the Git team is [`git-filter-repo`](https://github.com/newren/git-filter-repo).

### Step 1: Create a Fresh Mirror Backup
Always create a fresh bare mirror of your repository before rewriting history:
```bash
git clone --mirror https://github.com/Dev-VardhanX/VALUE.git VALUE-mirror-backup.git
```

### Step 2: Install `git-filter-repo`
Using Python / pip:
```bash
python -m pip install --user git-filter-repo
```

### Step 3: Run the Filter
You can either strip all historical blobs larger than 1 MB, or remove specific directories across all history:

#### Option A: Strip all blobs > 1 MB (Recommended)
```bash
# In your fresh local clone (NOT the mirror backup):
git filter-repo --strip-blobs-bigger-than 1M
```

#### Option B: Strip specific QA paths
```bash
git filter-repo --invert-paths \
  --path qa/traces \
  --path qa/smooth-before \
  --path qa/smooth-after \
  --path qa/previews
```

### Step 4: Expire and Garbage Collect
```bash
git reflog expire --expire=now --all
git gc --prune=now --aggressive
```
After running this, the `.git/` folder will shrink from ~36.6 MB down to under **2 MB**.

### Step 5: Force Push to Remote (When Ready)
```bash
# Re-add your remote origin if filter-repo removed it
git remote add origin https://github.com/Dev-VardhanX/VALUE.git

# Force-push updated branches and tags
git push origin --force --all
git push origin --force --tags
```

---

## 4. Policy Notice

Per repository safety protocols, **automated agents will never run history rewriting or force-pushes without explicit, dedicated user instructions**. The repository working tree is already 100% clean and optimized.
