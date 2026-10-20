package com.example.myapplication.util

object MoneyFormatter {
    fun formatPaise(paise: Long): String {
        val rupees = paise / 100
        val paiseRemainder = paise % 100
        
        return if (paiseRemainder == 0L) {
            "₹${formatWithCommas(rupees)}"
        } else {
            "₹${formatWithCommas(rupees)}.${String.format("%02d", paiseRemainder)}"
        }
    }
    
    private fun formatWithCommas(number: Long): String {
        return String.format("%,d", number)
    }
}
