# Fix Java Version Issue

## Problem
Your build is failing because:
- **Android Gradle Plugin 8.2.0 requires Java 11 or higher**
- Your system is currently using **Java 8** (1.8.0_341)

## Solution Options

### Option 1: Use Android Studio's Bundled JDK (Recommended)

If you have Android Studio installed, it comes with a bundled JDK (usually Java 17). You can configure Gradle to use it:

1. **Find Android Studio's JDK location:**
   - Usually at: `C:\Users\<username>\AppData\Local\Android\android-studio\jbr`
   - Or: `C:\Program Files\Android\Android Studio\jbr`

2. **Set JAVA_HOME temporarily for this session:**
   ```powershell
   $env:JAVA_HOME = "C:\Users\aks\AppData\Local\Android\android-studio\jbr"
   ```

3. **Or edit `gradle.properties` to specify the Java path:**
   ```properties
   org.gradle.java.home=C:\\Users\\aks\\AppData\\Local\\Android\\android-studio\\jbr
   ```
   (Note: Use double backslashes `\\` in gradle.properties)

### Option 2: Install Java 17 (Recommended for Long-term)

1. **Download Java 17:**
   - Visit: https://adoptium.net/ (Eclipse Temurin)
   - Download JDK 17 LTS for Windows x64
   - Install it

2. **Set JAVA_HOME:**
   ```powershell
   # Find where Java 17 was installed (usually):
   # C:\Program Files\Eclipse Adoptium\jdk-17.x.x
   
   # Set JAVA_HOME (PowerShell):
   $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.x"
   ```

3. **Add to System Environment Variables (permanent):**
   - Right-click "This PC" → Properties
   - Advanced system settings → Environment Variables
   - Under "User variables", add/edit `JAVA_HOME`
   - Set value to your Java 17 installation path

### Option 3: Use Java 11 (Minimum Required)

Same as Option 2, but install Java 11 instead of 17.

## Quick Fix for Now

**If you have Android Studio installed**, run this in PowerShell:

```powershell
# Try to find Android Studio JDK
$studioJdk = "$env:LOCALAPPDATA\Android\android-studio\jbr"
if (Test-Path $studioJdk) {
    $env:JAVA_HOME = $studioJdk
    Write-Host "Set JAVA_HOME to: $studioJdk"
    & "$studioJdk\bin\java.exe" -version
} else {
    Write-Host "Android Studio JDK not found at: $studioJdk"
    Write-Host "Please install Java 11+ or configure JAVA_HOME manually"
}
```

Then try building again:
```bash
bash run-on-emulator.sh
```

## Verify Java Version

After setting JAVA_HOME, verify:
```powershell
$env:JAVA_HOME\bin\java.exe -version
```

Should show version 11 or higher (17 is recommended).

## After Fixing

Once Java is configured correctly, the build should work. You can test with:
```bash
bash run-on-emulator.sh
```



