package com.ilmariware.currencyconverterwidget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.review.ReviewManagerFactory
import com.ilmariware.currencyconverterwidget.data.CurrencyRepository
import com.ilmariware.currencyconverterwidget.data.WidgetPreferences
import com.ilmariware.currencyconverterwidget.data.models.Currency
import com.ilmariware.currencyconverterwidget.data.models.UpdateFrequency
import com.ilmariware.currencyconverterwidget.data.models.WidgetTheme
import com.ilmariware.currencyconverterwidget.widget.CurrencyConverterWidget
import com.ilmariware.currencyconverterwidget.widget.WidgetUpdateWorker
import kotlinx.coroutines.launch

class WidgetConfigurationActivity : AppCompatActivity() {
    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var preferences: WidgetPreferences
    private lateinit var repository: CurrencyRepository

    private lateinit var sourceCurrencySpinner: Spinner
    private lateinit var targetCurrencySpinner: Spinner
    private lateinit var updateFrequencyRadioGroup: RadioGroup
    private lateinit var themeSpinner: Spinner
    private lateinit var addWidgetButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var seekBarOpacity: SeekBar
    private lateinit var opacityValueText: TextView
    private lateinit var previewContainer: FrameLayout
    private lateinit var widgetPreview: LinearLayout
    private lateinit var previewDisplayContainer: LinearLayout
    private lateinit var previewSourceLabel: TextView
    private lateinit var previewTargetLabel: TextView
    private lateinit var previewInputDisplay: TextView
    private lateinit var previewOutputDisplay: TextView
    private lateinit var previewTimestamp: TextView
    private lateinit var previewButtons: List<TextView>
    private lateinit var previewBackground: GradientDrawable
    private lateinit var titleText: TextView
    private var isReconfigure = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set result to CANCELED in case user backs out
        setResult(RESULT_CANCELED)
        
        setContentView(R.layout.activity_widget_configuration)
        
        preferences = WidgetPreferences(this)
        repository = CurrencyRepository(this)
        
        // Get widget ID from intent
        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        
        initViews()
        setupSpinners()
        setupThemeSpinner()
        loadExistingConfiguration()
        setupListeners()
        setupBackNavigation()
        updatePreview()
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                closeConfiguration()
            }
        })
    }

    private fun initViews() {
        titleText = findViewById(R.id.titleText)
        sourceCurrencySpinner = findViewById(R.id.sourceCurrencySpinner)
        targetCurrencySpinner = findViewById(R.id.targetCurrencySpinner)
        updateFrequencyRadioGroup = findViewById(R.id.updateFrequencyRadioGroup)
        themeSpinner = findViewById(R.id.themeSpinner)
        addWidgetButton = findViewById(R.id.addWidgetButton)
        progressBar = findViewById(R.id.progressBar)
        seekBarOpacity = findViewById(R.id.seekBarOpacity)
        opacityValueText = findViewById(R.id.opacityValueText)
        previewContainer = findViewById(R.id.previewContainer)
        widgetPreview = findViewById(R.id.widgetPreview)
        previewDisplayContainer = findViewById(R.id.previewDisplayContainer)
        previewSourceLabel = findViewById(R.id.previewSourceLabel)
        previewTargetLabel = findViewById(R.id.previewTargetLabel)
        previewInputDisplay = findViewById(R.id.previewInputDisplay)
        previewOutputDisplay = findViewById(R.id.previewOutputDisplay)
        previewTimestamp = findViewById(R.id.previewTimestamp)
        previewButtons = listOf(
            findViewById(R.id.previewBtn1), findViewById(R.id.previewBtn2),
            findViewById(R.id.previewBtn3), findViewById(R.id.previewBtn4),
            findViewById(R.id.previewBtn5), findViewById(R.id.previewBtn6)
        )

        val density = resources.displayMetrics.density
        previewContainer.background = CheckerboardDrawable(tileSizePx = 20f * density)

        previewBackground = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 24f
        }
        widgetPreview.background = previewBackground
    }

    /** Draws a classic grey/white checkerboard pattern to make transparency clearly visible. */
    private class CheckerboardDrawable(private val tileSizePx: Float = 40f) : Drawable() {
        private val lightPaint = Paint().apply { color = Color.parseColor("#CCCCCC") }
        private val darkPaint = Paint().apply { color = Color.parseColor("#888888") }

        override fun draw(canvas: Canvas) {
            val cols = (bounds.width() / tileSizePx).toInt() + 2
            val rows = (bounds.height() / tileSizePx).toInt() + 2
            for (row in 0..rows) {
                for (col in 0..cols) {
                    val paint = if ((row + col) % 2 == 0) lightPaint else darkPaint
                    canvas.drawRect(
                        bounds.left + col * tileSizePx,
                        bounds.top + row * tileSizePx,
                        bounds.left + (col + 1) * tileSizePx,
                        bounds.top + (row + 1) * tileSizePx,
                        paint
                    )
                }
            }
        }

        override fun setAlpha(alpha: Int) {}
        override fun setColorFilter(cf: android.graphics.ColorFilter?) {}
        @Deprecated("Deprecated in Java")
        override fun getOpacity() = android.graphics.PixelFormat.OPAQUE
    }

    private fun updatePreview() {
        val theme = WidgetTheme.values()[themeSpinner.selectedItemPosition]
        val transparency = seekBarOpacity.progress
        val alpha = ((100 - transparency) / 100f * 255).toInt()
        val colorWithAlpha = ColorUtils.setAlphaComponent(theme.backgroundColor, alpha)

        previewBackground.setColor(colorWithAlpha)

        previewDisplayContainer.setBackgroundResource(theme.displayBackgroundDrawable)

        previewSourceLabel.setTextColor(theme.textColor)
        previewInputDisplay.setTextColor(theme.textColor)
        previewTargetLabel.setTextColor(theme.targetTextColor)
        previewOutputDisplay.setTextColor(theme.targetTextColor)

        if (transparency > 0) {
            previewTimestamp.setTextColor(theme.buttonTextColor)
        } else {
            previewTimestamp.setTextColor(theme.timestampColor)
        }

        previewButtons.forEach { btn ->
            btn.setBackgroundResource(theme.buttonBackgroundDrawable)
            btn.setTextColor(theme.buttonTextColor)
        }

        opacityValueText.text = "$transparency%"
    }

    private fun setupSpinners() {
        val sourceAdapter = CurrencySpinnerAdapter(this, Currency.values().toList())
        val targetAdapter = CurrencySpinnerAdapter(this, Currency.values().toList())
        
        sourceCurrencySpinner.adapter = sourceAdapter
        targetCurrencySpinner.adapter = targetAdapter
    }

    private fun loadExistingConfiguration() {
        isReconfigure = preferences.hasConfig(widgetId)
        if (isReconfigure) {
            titleText.setText(R.string.edit_widget_title)
            addWidgetButton.setText(R.string.save_changes)
        }

        sourceCurrencySpinner.setSelection(preferences.getSourceCurrency(widgetId).ordinal)
        targetCurrencySpinner.setSelection(preferences.getTargetCurrency(widgetId).ordinal)
        themeSpinner.setSelection(preferences.getTheme(widgetId).ordinal)
        seekBarOpacity.progress = 100 - preferences.getOpacity(widgetId)

        val frequencyRadioId = when (preferences.getUpdateFrequency(widgetId)) {
            UpdateFrequency.TWELVE_HOURS -> R.id.radio12Hours
            UpdateFrequency.WEEKLY -> R.id.radioWeekly
            else -> R.id.radioDaily
        }
        updateFrequencyRadioGroup.check(frequencyRadioId)
    }

    private fun setupThemeSpinner() {
        val themes = WidgetTheme.values()
        val adapter = ThemeSpinnerAdapter(this, themes.toList())

        themeSpinner.adapter = adapter
        themeSpinner.setSelection(0)
        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updatePreview()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
    
    // Custom adapter for grouped currency display
    private class CurrencySpinnerAdapter(
        context: Context,
        private val currencies: List<Currency>
    ) : ArrayAdapter<Currency>(context, 0, currencies) {
        
        private val commonCurrencies = Currency.getCommonCurrencies()
        
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val currency = currencies[position]
            val view = convertView ?: LayoutInflater.from(context).inflate(
                R.layout.currency_spinner_item, parent, false
            )
            
            val currencyText = view.findViewById<TextView>(R.id.currencyText)
            currencyText.text = "${currency.code} - ${currency.displayName}"
            
            return view
        }
        
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val currency = currencies[position]
            
            // Check if this is the first item of a new section
            val isFirstCommon = position == 0
            val isFirstOther = position == commonCurrencies.size
            
            if (isFirstCommon || isFirstOther) {
                // Create container with header and item
                val linearLayout = android.widget.LinearLayout(context).apply {
                    orientation = android.widget.LinearLayout.VERTICAL
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
                
                // Add header
                val header = LayoutInflater.from(context).inflate(
                    R.layout.currency_spinner_header, linearLayout, false
                ) as TextView
                header.text = if (isFirstCommon) "COMMON CURRENCIES" else "ALL CURRENCIES"
                linearLayout.addView(header)
                
                // Add currency item
                val itemView = LayoutInflater.from(context).inflate(
                    R.layout.currency_spinner_item, linearLayout, false
                )
                val currencyText = itemView.findViewById<TextView>(R.id.currencyText)
                currencyText.text = "${currency.code} - ${currency.displayName}"
                linearLayout.addView(itemView)
                
                return linearLayout
            } else {
                // Regular item without header
                val view = LayoutInflater.from(context).inflate(
                    R.layout.currency_spinner_item, parent, false
                )
                
                val currencyText = view.findViewById<TextView>(R.id.currencyText)
                currencyText.text = "${currency.code} - ${currency.displayName}"
                
                return view
            }
        }
    }
    
    // Custom adapter to show color preview
    private class ThemeSpinnerAdapter(
        context: Context,
        private val themes: List<WidgetTheme>
    ) : ArrayAdapter<WidgetTheme>(context, 0, themes) {
        
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return createView(position, convertView, parent)
        }
        
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return createView(position, convertView, parent)
        }
        
        private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(
                R.layout.theme_spinner_item, parent, false
            )
            
            val theme = themes[position]
            val colorPreview = view.findViewById<View>(R.id.colorPreview)
            val themeName = view.findViewById<TextView>(R.id.themeName)
            
            // Create rounded rectangle with theme color
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                // setColor(theme.backgroundColor)
                // Use a more "vibrant" preview by increasing saturation and value
                val hsv = FloatArray(3)
                Color.colorToHSV(theme.backgroundColor, hsv)
                hsv[1] = (hsv[1] * 1.25f).coerceAtMost(1f) // Boost saturation
                hsv[2] = (hsv[2] * 1.18f).coerceAtMost(1f) // Boost value (brightness)
                setColor(Color.HSVToColor(hsv))
                cornerRadius = 16f
                setStroke(2, 0xFFDDDDDD.toInt())
            }
            colorPreview.background = drawable
            themeName.text = theme.displayName
            
            return view
        }
    }

    private fun setupListeners() {
        addWidgetButton.setOnClickListener {
            saveConfigurationAndFinish()
        }

        seekBarOpacity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun saveConfigurationAndFinish() {
        val sourceCurrency = Currency.values()[sourceCurrencySpinner.selectedItemPosition]
        val targetCurrency = Currency.values()[targetCurrencySpinner.selectedItemPosition]
        
        if (sourceCurrency == targetCurrency) {
            // Show error - same currencies
            android.widget.Toast.makeText(
                this,
                "Please select different currencies",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        val updateFrequency = when (updateFrequencyRadioGroup.checkedRadioButtonId) {
            R.id.radio12Hours -> UpdateFrequency.TWELVE_HOURS
            R.id.radioWeekly -> UpdateFrequency.WEEKLY
            else -> UpdateFrequency.DAILY
        }
        
        val selectedTheme = WidgetTheme.values()[themeSpinner.selectedItemPosition]
        val opacity = 100 - seekBarOpacity.progress
        val previousSource = preferences.getSourceCurrency(widgetId)
        val previousTarget = preferences.getTargetCurrency(widgetId)
        val currenciesChanged = previousSource != sourceCurrency || previousTarget != targetCurrency

        // Show loading
        addWidgetButton.isEnabled = false
        progressBar.visibility = View.VISIBLE

        // Save configuration
        preferences.setSourceCurrency(widgetId, sourceCurrency)
        preferences.setTargetCurrency(widgetId, targetCurrency)
        preferences.setUpdateFrequency(widgetId, updateFrequency)
        preferences.setTheme(widgetId, selectedTheme)
        preferences.setOpacity(widgetId, opacity)
        if (!isReconfigure) {
            preferences.setCurrentInput(widgetId, "0")
        }

        // Fetch initial exchange rate (skip when only theme/frequency/opacity changed)
        lifecycleScope.launch {
            try {
                val fetchError = if (!isReconfigure || currenciesChanged) {
                    val result = repository.getExchangeRate(sourceCurrency, targetCurrency, forceRefresh = true)
                    if (result.isSuccess || preferences.getCachedRate(sourceCurrency, targetCurrency) != null) {
                        null
                    } else {
                        result.exceptionOrNull()?.message ?: "Unknown error"
                    }
                } else {
                    null
                }

                if (fetchError == null) {
                    // Schedule periodic updates
                    WidgetUpdateWorker.scheduleUpdate(applicationContext, widgetId, updateFrequency)

                    // Update the widget
                    val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
                    CurrencyConverterWidget.updateWidget(
                        applicationContext,
                        appWidgetManager,
                        widgetId
                    )

                    finishAfterSuccessfulSave()
                } else {
                    // Failed to get rate
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        addWidgetButton.isEnabled = true
                        android.widget.Toast.makeText(
                            this@WidgetConfigurationActivity,
                            "Failed to fetch exchange rate: ${fetchError}. Please try again.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    addWidgetButton.isEnabled = true
                    android.widget.Toast.makeText(
                        this@WidgetConfigurationActivity,
                        "Error: ${e.message}. Please check your internet connection and try again.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun finishAfterSuccessfulSave() {
        if (!isReconfigure) {
            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }
            setResult(RESULT_OK, resultValue)
            finish()
            return
        }

        preferences.recordSuccessfulEdit().let { edits ->
            if (preferences.shouldPromptInAppReview(edits)) {
                progressBar.visibility = View.GONE
                addWidgetButton.isEnabled = true
                preferences.markInAppReviewPrompted()
                launchInAppReviewThenClose()
            } else {
                closeConfiguration()
            }
        }
    }

    private fun launchInAppReviewThenClose() {
        val manager = ReviewManagerFactory.create(this)
        manager.requestReviewFlow().addOnCompleteListener { request ->
            if (isDestroyed) return@addOnCompleteListener
            if (!request.isSuccessful) {
                closeConfiguration()
                return@addOnCompleteListener
            }
            manager.launchReviewFlow(this, request.result).addOnCompleteListener {
                closeConfiguration()
            }
        }
    }

    private fun closeConfiguration() {
        if (isDestroyed) return
        if (isReconfigure) {
            finishAndRemoveTask()
        } else {
            finish()
        }
    }
}

