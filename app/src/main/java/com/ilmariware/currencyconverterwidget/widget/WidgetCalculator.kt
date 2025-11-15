package com.ilmariware.currencyconverterwidget.widget

class WidgetCalculator {
    
    /**
     * Handles calculator button input and returns the new input string
     */
    fun handleInput(currentInput: String, buttonValue: String): String {
        return when (buttonValue) {
            "C" -> "0"
            "⌫" -> handleBackspace(currentInput)
            "." -> handleDot(currentInput)
            "000" -> handle000(currentInput)
            in "0".."9" -> handleDigit(currentInput, buttonValue)
            else -> currentInput
        }
    }

    private fun handleDigit(currentInput: String, digit: String): String {
        // If current input is "0", replace it (unless there's already a decimal point)
        if (currentInput == "0") {
            return digit
        }
        
        // Limit total length to prevent overflow
        if (currentInput.length >= MAX_INPUT_LENGTH) {
            return currentInput
        }
        
        return currentInput + digit
    }

    private fun handleDot(currentInput: String): String {
        // Don't add dot if there's already one
        if (currentInput.contains(".")) {
            return currentInput
        }
        
        // If input is empty or "0", add "0." 
        if (currentInput.isEmpty() || currentInput == "0") {
            return "0."
        }
        
        // Limit total length
        if (currentInput.length >= MAX_INPUT_LENGTH) {
            return currentInput
        }
        
        return "$currentInput."
    }

    private fun handleBackspace(currentInput: String): String {
        // If only one character or "0", return "0"
        if (currentInput.length <= 1 || currentInput == "0") {
            return "0"
        }
        
        // Remove last character
        val newInput = currentInput.dropLast(1)
        
        // If result is empty, return "0"
        return if (newInput.isEmpty()) "0" else newInput
    }

    private fun handle000(currentInput: String): String {
        // Don't add if current input is just "0"
        if (currentInput == "0") {
            return currentInput
        }
        
        // Limit total length
        if (currentInput.length + 3 > MAX_INPUT_LENGTH) {
            return currentInput
        }
        
        return currentInput + "000"
    }

    companion object {
        private const val MAX_INPUT_LENGTH = 12
    }
}

