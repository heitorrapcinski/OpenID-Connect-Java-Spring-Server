# Windows Environment

This workspace runs on **Windows** with **PowerShell** as the default shell.

## Shell & Command Rules

- Always use PowerShell syntax for all shell commands
- Never use bash/Unix commands (e.g., `&&`, `rm`, `ls`, `cat`, `mkdir` with Unix flags)
- Use PowerShell equivalents:
  - `&&` → `;`
  - `rm -rf` → `Remove-Item -Recurse -Force`
  - `ls` → `Get-ChildItem`
  - `cat` → `Get-Content`
  - `mkdir` → `New-Item -ItemType Directory -Path`
  - `cp -r` → `Copy-Item -Recurse`
  - `mv` → `Move-Item`
  - `echo` → `Write-Output`
  - `grep` → `Select-String`
  - `export VAR=value` → `$env:VAR = "value"`
  - `touch file` → `New-Item -ItemType File -Path file`

## Path Conventions

- Use backslashes `\` or forward slashes `/` (PowerShell accepts both)
- Avoid Unix-style absolute paths like `/usr/local/...`

## Package Managers

- Node.js: `npm` or `yarn` commands work normally in PowerShell
- Python: use `pip` or `uv` as available
- Java: use `mvn` or `gradle` as available
