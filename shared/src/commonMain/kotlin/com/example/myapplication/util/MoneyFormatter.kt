package com.example.myapplication.util

object MoneyFormatter {
    fun formatPaise(paise: Long): String {
        val rupees = paise / 100
        val paiseRemainder = paise % 100
        
        return if (paiseRemainder == 0L) {
            "₹${formatWithCommas(rupees)}"
        } else {
            val remStr = if (paiseRemainder < 10) "0$paiseRemainder" else "$paiseRemainder"
            "₹${formatWithCommas(rupees)}.$remStr"
        }
    }
    
    private fun formatWithCommas(number: Long): String {
        val str = number.toString()
        val len = str.length
        if (len <= 3) return str
        
        val sb = StringBuilder()
        var count = 0
        for (i in len - 1 downTo 0) {
            sb.append(str[i])
            count++
            if (i > 0 && count == 3) {
                sb.append(',')
            } else if (i > 0 && count > 3 && (count - 3) % 2 == 0) {
                sb.append(',')
            }
        }
        return sb.reverse().toString()
    }
}
