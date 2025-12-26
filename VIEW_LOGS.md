# How to View Console Logs (Token Usage & Costs)

The app logs token usage and costs to the Android console. Here are several ways to view them:

## Method 1: Android Studio Logcat (Recommended)

1. **Open Android Studio**
2. **Run the app** on an emulator or device
3. **Open Logcat**:
   - Bottom panel → "Logcat" tab
   - Or: View → Tool Windows → Logcat
4. **Filter logs**:
   - In the search box, type: `OpenAIEmbeddingsClient` or `OpenAIUsageTracker`
   - Or filter by tag: `tag:OpenAIEmbeddingsClient`
   - Set log level to **Info** or **Verbose** (not just Error/Warn)

## Method 2: Command Line (adb logcat)

### Windows (PowerShell/CMD):
```powershell
# Connect to device/emulator first
adb devices

# View all logs
adb logcat

# Filter for OpenAI usage logs only
adb logcat | Select-String "OpenAI"

# Filter by tag
adb logcat -s OpenAIEmbeddingsClient:I OpenAIUsageTracker:I

# Clear logs and show new ones
adb logcat -c && adb logcat | Select-String "OpenAI"
```

### Linux/Mac:
```bash
# View all logs
adb logcat

# Filter for OpenAI usage logs only
adb logcat | grep -i "OpenAI"

# Filter by tag
adb logcat -s OpenAIEmbeddingsClient:I OpenAIUsageTracker:I

# Clear logs and show new ones
adb logcat -c && adb logcat | grep -i "OpenAI"
```

## Method 3: Filter by Log Level

The token usage logs use **INFO** level (`Log.i()`), so make sure your filter includes INFO logs:

### In Android Studio Logcat:
- Click the log level dropdown (usually shows "Verbose")
- Select "Info" or "Verbose" (not just "Error" or "Warn")

### In adb logcat:
```bash
# Show Info level and above
adb logcat *:I

# Show only specific tags at Info level
adb logcat OpenAIEmbeddingsClient:I OpenAIUsageTracker:I *:S
```

## What You'll See

When you make a query, you should see logs like:

```
═══════════════════════════════════════════════════════
OpenAI API Usage - Embeddings
Model: text-embedding-3-small
Prompt Tokens: 15
Total Tokens: 15
Cost: $0.000000
═══════════════════════════════════════════════════════

═══════════════════════════════════════════════════════════════════
OpenAI API Usage - Session Summary
═══════════════════════════════════════════════════════════════════

Model: text-embedding-3-small
  Requests: 1
  Prompt Tokens: 15
  Total Tokens: 15
  Cost: $0.000000

═══════════════════════════════════════════════════════════════════
TOTAL SESSION:
  Total Requests: 1
  Total Tokens: 15
  Total Cost: $0.000000
═══════════════════════════════════════════════════════════════════
```

## Troubleshooting

### If you don't see any logs:

1. **Check if the app is making API calls**:
   - Make sure you have an OpenAI API key configured
   - Make sure you're connected to the internet
   - Try entering a query in the app

2. **Check log level filter**:
   - Make sure INFO level logs are visible
   - Try setting to "Verbose" to see all logs

3. **Check device/emulator connection**:
   ```bash
   adb devices
   ```
   Should show your device/emulator

4. **Clear and restart**:
   ```bash
   adb logcat -c
   # Then use the app again
   ```

5. **Check if logs are being generated**:
   ```bash
   # View ALL logs to see if anything is coming through
   adb logcat
   ```

## Quick Test

To verify logging is working, you can also check for any log with the tag:
```bash
adb logcat | grep -i "OpenAIEmbeddingsClient\|OpenAIUsageTracker"
```

Or in Android Studio Logcat, search for: `OpenAI`

