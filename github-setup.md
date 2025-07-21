# GitHub Repository Setup Instructions

Follow these steps to push your project to GitHub:

## 1. Open Terminal/Command Prompt

Open a terminal or command prompt in your project root directory:
```bash
cd c:/Users/bbk04/Desktop/Bogendra/Projects/store-management-system
```

## 2. Initialize Git Repository (if not already done)
```bash
git init
```

## 3. Create a .gitignore file

Let's create a basic .gitignore file for Java and Node.js projects:

```bash
# Create .gitignore file
echo "# Compiled class files
*.class

# Log files
*.log

# Package files
*.jar
*.war
*.ear
*.zip

# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties

# Node.js
node_modules/
npm-debug.log
yarn-debug.log
yarn-error.log
.env
.env.local
.env.development.local
.env.test.local
.env.production.local

# IDE files
.idea/
*.iml
*.iws
*.ipr
.classpath
.project
.settings/
.vscode/

# Misc
.DS_Store
Thumbs.db
" > .gitignore
```

## 4. Add your GitHub Repository URL

Replace YOUR_USERNAME with your GitHub username and REPO_NAME with your repository name:
```bash
git remote add origin https://github.com/YOUR_USERNAME/REPO_NAME.git
```

## 5. Add, Commit and Push your code
```bash
# Add all files
git add .

# Commit changes
git commit -m "Initial commit: Store Management System with Inventory Service integration"

# Push to GitHub
git push -u origin master
# Note: If your main branch is called "main" instead of "master", use:
# git push -u origin main
```

During the push, you'll be prompted for your GitHub credentials.

## 6. Verify Repository

After pushing, visit your GitHub repository URL to verify that all files have been uploaded successfully.
