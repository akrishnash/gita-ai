# Quick Install Guide

## 📱 Build and Install Scripts

After making changes to your app, use these scripts to quickly build and install on your phone.

## Available Scripts

### Windows (PowerShell/Batch)

### 1. **build-and-install.ps1** (Main Script)
Builds the app and installs it to your connected phone.

**Usage:**
```powershell
.\build-and-install.ps1
```

**What it does:**
- ✅ Checks for connected Android device
- ✅ Builds the debug APK
- ✅ Installs to your phone
- ✅ Shows success/error messages

### 2. **build-and-install.bat** (Double-Click Version)
Same as above, but you can double-click it in Windows Explorer.

**Usage:**
- Just double-click the file!

### 3. **quick-install.ps1** (Fast Install)
Installs existing APK without rebuilding (faster when you just want to reinstall).

**Usage:**
```powershell
.\quick-install.ps1
```

**When to use:**
- You already built the APK
- You just want to reinstall
- Faster than full build

### 4. **rebuild-install.ps1** (Clean Install)
Uninstalls old version, rebuilds, and installs fresh.

**Usage:**
```powershell
.\rebuild-install.ps1
```

**When to use:**
- You want a completely clean install
- App is behaving strangely
- After major changes

### Linux/Mac (Bash)

#### 1. **build-and-install.sh** (Main Script)
Builds the app and installs it to your connected phone.

**Usage:**
```bash
chmod +x build-and-install.sh
./build-and-install.sh
```

**Options:**
- `--skip-build` - Install only (skip build)
- `--uninstall-first` - Uninstall old version first

**Example:**
```bash
./build-and-install.sh --skip-build          # Quick install
./build-and-install.sh --uninstall-first     # Clean install
```

#### 2. **quick-install.sh** (Fast Install)
Installs existing APK without rebuilding.

**Usage:**
```bash
chmod +x quick-install.sh
./quick-install.sh
```

#### 3. **rebuild-install.sh** (Clean Install)
Uninstalls, rebuilds, and installs fresh.

**Usage:**
```bash
chmod +x rebuild-install.sh
./rebuild-install.sh
```

**Note:** The script will automatically detect `ANDROID_HOME` or try common SDK locations:
- `$HOME/Android/Sdk` (Linux)
- `$HOME/Library/Android/sdk` (Mac)

## 🔧 Setup (First Time Only)

### 1. Enable USB Debugging on Your Phone

1. Go to **Settings** → **About Phone**
2. Tap **Build Number** 7 times (enables Developer Options)
3. Go back to **Settings** → **Developer Options**
4. Enable **USB Debugging**

### 2. Connect Your Phone

1. Connect phone via USB cable
2. On your phone, accept the "Allow USB Debugging" prompt
3. Check "Always allow from this computer" if you want

### 3. Verify Connection

Run this to check:
```powershell
adb devices
```

You should see your device listed.

## 📝 Workflow

**Typical development cycle:**

1. Make changes to your code
2. Run: `.\build-and-install.ps1`
3. Test on your phone
4. Repeat!

**Quick iteration:**
- Make small changes → `.\quick-install.ps1` (faster)
- Make big changes → `.\build-and-install.ps1` (full build)

## ⚠️ Troubleshooting

### "No Android device found"
- Check USB cable connection
- Enable USB Debugging
- Accept the USB debugging prompt on phone
- Try: `adb devices` to verify

### "Build failed"
- Check for compilation errors
- Ensure Android SDK is properly configured
- Check `local.properties` has correct SDK path

### "Installation failed"
- Uninstall old version first: `adb uninstall com.gita.app`
- Or use: `.\rebuild-install.ps1`

### Permission Denied
- Make sure you accepted USB debugging prompt
- Try unplugging and reconnecting USB cable

## 💡 Tips

- Keep your phone connected while developing
- Use `quick-install.ps1` for faster iteration
- Use `rebuild-install.ps1` if app acts weird
- Check `adb devices` if connection issues

## 🚀 Quick Start

1. Connect your phone
2. Double-click `build-and-install.bat`
3. Wait for "Installation successful!"
4. Open the app on your phone!

---

**Happy coding! 🎉**

