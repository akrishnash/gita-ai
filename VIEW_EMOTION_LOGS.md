# View Emotion Detection Logs

This guide shows you how to view the detected emotions in the console/logs.

## Quick Start

### Windows
```bash
view-emotion-logs.bat
```

### Linux/Mac/Git Bash
```bash
./view-emotion-logs.sh
```

## What You'll See

When you enter a query in the app, you'll see:

1. **In the App UI**: A prominent card at the top showing:
   - Detected Emotion (large, bold text)
   - Confidence percentage

2. **In Console/Logs**: Detailed information including:
   - User query
   - Closest emotion detected
   - Emotion score
   - Top 5 emotion scores

## Example Log Output

```
╔═══════════════════════════════════════════════════════════╗
║                    DETECTED EMOTION                       ║
╠═══════════════════════════════════════════════════════════╣
║  User Query: I'm feeling anxious about work
║  
║  🎯 CLOSEST EMOTION: Anxiety
║  📊 Emotion Score: 0.8234
║  
║  All Emotion Scores:
║    • Anxiety: 0.8234
║    • Grief: 0.7123
║    • Burnout: 0.6891
║    • Moral Dilemma: 0.6543
║    • Identity Crisis: 0.6123
╚═══════════════════════════════════════════════════════════╝
```

## Alternative: View All Logs

If you want to see all app logs (not just emotion detection):

### Windows
```bash
adb logcat -s KotlinModelRepository:I MainViewModel:I
```

### Linux/Mac
```bash
adb logcat -s KotlinModelRepository:I MainViewModel:I
```

## Troubleshooting

**No device found?**
- Make sure your phone/emulator is connected
- Enable USB debugging on your phone
- Check with: `adb devices`

**Can't see logs?**
- Make sure the app is running
- Try clearing logs first: `adb logcat -c`
- Check that you have the latest version of the app installed

