package com.ilmariware.currencyconverterwidget.data.models

import android.graphics.Color
import com.ilmariware.currencyconverterwidget.R

enum class WidgetTheme(
    val displayName: String,
    val backgroundColor: Int,
    val displayBackgroundDrawable: Int,
    val buttonBackgroundDrawable: Int,
    val textColor: Int,
    val targetTextColor: Int,
    val buttonTextColor: Int,
    val timestampColor: Int
) {
    CLASSIC(
        displayName = "Classic",
        backgroundColor = Color.parseColor("#F5F5F5"),
        displayBackgroundDrawable = R.drawable.display_classic,
        buttonBackgroundDrawable = R.drawable.button_classic,
        textColor = Color.parseColor("#000000"),
        targetTextColor = Color.parseColor("#1976D2"),
        buttonTextColor = Color.parseColor("#000000"),
        timestampColor = Color.parseColor("#999999")
    ),
    
    DARK(
        displayName = "Dark",
        backgroundColor = Color.parseColor("#212121"),
        displayBackgroundDrawable = R.drawable.display_dark,
        buttonBackgroundDrawable = R.drawable.button_dark,
        textColor = Color.parseColor("#FFFFFF"),
        targetTextColor = Color.parseColor("#64B5F6"),
        buttonTextColor = Color.parseColor("#FFFFFF"),
        timestampColor = Color.parseColor("#AAAAAA")
    ),
    
    OCEAN(
        displayName = "Ocean Blue",
        backgroundColor = Color.parseColor("#E3F2FD"),
        displayBackgroundDrawable = R.drawable.display_ocean,
        buttonBackgroundDrawable = R.drawable.button_ocean,
        textColor = Color.parseColor("#0D47A1"),
        targetTextColor = Color.parseColor("#1976D2"),
        buttonTextColor = Color.parseColor("#0D47A1"),
        timestampColor = Color.parseColor("#64B5F6")
    ),
    
    MINT(
        displayName = "Mint Green",
        backgroundColor = Color.parseColor("#E8F5E9"),
        displayBackgroundDrawable = R.drawable.display_mint,
        buttonBackgroundDrawable = R.drawable.button_mint,
        textColor = Color.parseColor("#1B5E20"),
        targetTextColor = Color.parseColor("#388E3C"),
        buttonTextColor = Color.parseColor("#1B5E20"),
        timestampColor = Color.parseColor("#81C784")
    ),
    
    SUNSET(
        displayName = "Sunset",
        backgroundColor = Color.parseColor("#FFF3E0"),
        displayBackgroundDrawable = R.drawable.display_sunset,
        buttonBackgroundDrawable = R.drawable.button_sunset,
        textColor = Color.parseColor("#E65100"),
        targetTextColor = Color.parseColor("#F57C00"),
        buttonTextColor = Color.parseColor("#E65100"),
        timestampColor = Color.parseColor("#FFB74D")
    ),

    PURPLE(
        displayName = "Purple",
        backgroundColor = Color.parseColor("#F3E5F5"),
        displayBackgroundDrawable = R.drawable.display_purple,
        buttonBackgroundDrawable = R.drawable.button_purple,
        textColor = Color.parseColor("#4A148C"),
        targetTextColor = Color.parseColor("#6A1B9A"),
        buttonTextColor = Color.parseColor("#4A148C"),
        timestampColor = Color.parseColor("#9575CD")
    );

    companion object {
        fun fromOrdinal(ordinal: Int): WidgetTheme {
            return values().getOrNull(ordinal) ?: CLASSIC
        }
    }
}

