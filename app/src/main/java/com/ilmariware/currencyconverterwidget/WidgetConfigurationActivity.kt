package com.ilmariware.currencyconverterwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ilmariware.currencyconverterwidget.data.CurrencyRepository
import com.ilmariware.currencyconverterwidget.data.WidgetPreferences
import com.ilmariware.currencyconverterwidget.data.models.Currency
import com.ilmariware.currencyconverterwidget.data.models.UpdateFrequency
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
        setupListeners()
    }

    private fun initViews() {
        sourceCurrencySpinner = findViewById(R.id.sourceCurrencySpinner)
        targetCurrencySpinner = findViewById(R.id.targetCurrencySpinner)
        updateFrequencyRadioGroup = findViewById(R.id.updateFrequencyRadioGroup)
        themeSpinner = findViewById(R.id.themeSpinner)
        addWidgetButton = findViewById(R.id.addWidgetButton)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupSpinners() {
        val sourceAdapter = CurrencySpinnerAdapter(this, Currency.values().toList())
        val targetAdapter = CurrencySpinnerAdapter(this, Currency.values().toList())
        
        sourceCurrencySpinner.adapter = sourceAdapter
        targetCurrencySpinner.adapter = targetAdapter
        
        // Set default selections
        sourceCurrencySpinner.setSelection(0) // USD
        targetCurrencySpinner.setSelection(1) // EUR
    }

    private fun setupThemeSpinner() {
        val themes = com.ilmariware.currencyconverterwidget.data.models.WidgetTheme.values()
        val adapter = ThemeSpinnerAdapter(this, themes.toList())
        
        themeSpinner.adapter = adapter
        themeSpinner.setSelection(0) // Classic theme by default
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
        private val themes: List<com.ilmariware.currencyconverterwidget.data.models.WidgetTheme>
    ) : ArrayAdapter<com.ilmariware.currencyconverterwidget.data.models.WidgetTheme>(context, 0, themes) {
        
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
        
        val selectedTheme = com.ilmariware.currencyconverterwidget.data.models.WidgetTheme.values()[themeSpinner.selectedItemPosition]
        
        // Show loading
        addWidgetButton.isEnabled = false
        progressBar.visibility = View.VISIBLE
        
        // Save configuration
        preferences.setSourceCurrency(widgetId, sourceCurrency)
        preferences.setTargetCurrency(widgetId, targetCurrency)
        preferences.setUpdateFrequency(widgetId, updateFrequency)
        preferences.setTheme(widgetId, selectedTheme)
        preferences.setCurrentInput(widgetId, "0")
        
        // Fetch initial exchange rate
        lifecycleScope.launch {
            try {
                val result = repository.getExchangeRate(sourceCurrency, targetCurrency, forceRefresh = true)
                
                if (result.isSuccess) {
                    // Schedule periodic updates
                    WidgetUpdateWorker.scheduleUpdate(applicationContext, widgetId, updateFrequency)
                    
                    // Update the widget
                    val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
                    CurrencyConverterWidget.updateWidget(
                        applicationContext,
                        appWidgetManager,
                        widgetId
                    )
                    
                    // Return success
                    val resultValue = Intent().apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    }
                    setResult(RESULT_OK, resultValue)
                    finish()
                } else {
                    // Failed to get rate
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        addWidgetButton.isEnabled = true
                        android.widget.Toast.makeText(
                            this@WidgetConfigurationActivity,
                            "Failed to fetch exchange rate: ${result.exceptionOrNull()?.message}. Please try again.",
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
}

