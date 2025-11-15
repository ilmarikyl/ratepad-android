## Important Constraints & Gotchas

### RemoteViews Limitations

Widgets use `RemoteViews` which has some restrictions:

⚠️ **CRITICAL**: The widget layout (`widget_layout.xml`) is at the MAXIMUM complexity that RemoteViews can handle:

- 4 TextViews (display + labels + timestamp)
- 13 Buttons (number pad)
- Simple LinearLayout nesting only

**DO NOT**:

- Add more views without testing extensively
- Use GridLayout (causes "Can't load widget" on some devices)
- Use complex nested layouts (>3-4 levels deep)
- Use custom styles or themes on buttons (breaks rendering)
- Use `minHeight`/`minWidth`/`padding` attributes on buttons

**SAFE PRACTICES**:

- Use plain `Button` widgets (no MaterialButton)
- Use `android:layout_margin` on buttons (not padding)
- Keep layout flat with simple LinearLayouts
- Test on actual device after ANY layout changes

### Testing Widget Changes

After modifying layouts:

1. Rebuild app in Android Studio
2. **Force close the app** (important!)
3. Remove ALL existing widgets from home screen
4. Add widget again
5. Check Logcat for "CurrencyConverterWidget" messages

Common error: "Can't load widget" = layout too complex for RemoteViews

### Button Click Handling

Widgets can't use regular onClick - they use broadcasts:

1. Each button has a PendingIntent with unique request code
2. Intent broadcasts to `CurrencyConverterWidget.onReceive()`
3. Widget extracts button value and updates calculator state
4. Widget re-renders with new values
