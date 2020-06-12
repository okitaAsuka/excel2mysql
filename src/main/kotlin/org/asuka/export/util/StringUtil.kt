package org.asuka.export.util

/**
 * @description: TODO
 * @author: asuka
 * @Date: 2020-06-12
 */
object StringUtil {

    fun isNumber(str: String): Boolean {
        if (str.length == 0) {
            return false
        }
        var sz = str.length
        var hasExp = false
        var hasDecPoint = false
        var allowSigns = false
        var foundDigit = false
        // deal with any possible sign up front
        val start = if (str[0] == '-') 1 else 0
        if (sz > start + 1) {
            if (str[start] == '0' && str[start + 1] == 'x') {
                var i = start + 2
                if (i == sz) {
                    return false // str == "0x"
                }
                // checking hex (it can't be anything else)
                while (i < str.length) {
                    val ch = str[i]
                    if ((ch < '0' || ch > '9')
                            && (ch < 'a' || ch > 'f')
                            && (ch < 'A' || ch > 'F')) {
                        return false
                    }
                    i++
                }
                return true
            }
        }
        sz-- // don't want to loop to the last char, check it afterwords
        // for type qualifiers
        var i = start
        // loop to the next to last char or to the last char if we need another digit to
        // make a valid number (e.g. chars[0..5] = "1234E")
        while (i < sz || i < sz + 1 && allowSigns && !foundDigit) {
            val ch = str[i]
            if (ch >= '0' && ch <= '9') {
                foundDigit = true
                allowSigns = false
            } else if (ch == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false
                }
                hasDecPoint = true
            } else if (ch == 'e' || ch == 'E') {
                // we've already taken care of hex.
                if (hasExp) {
                    // two E's
                    return false
                }
                if (!foundDigit) {
                    return false
                }
                hasExp = true
                allowSigns = true
            } else if (ch == '+' || ch == '-') {
                if (!allowSigns) {
                    return false
                }
                allowSigns = false
                foundDigit = false // we need a digit after the E
            } else {
                return false
            }
            i++
        }
        if (i < str.length) {
            val ch = str[i]
            if (ch >= '0' && ch <= '9') {
                // no type qualifier, OK
                return true
            }
            if (ch == 'e' || ch == 'E') {
                // can't have an E at the last byte
                return false
            }
            if (!allowSigns
                    && (ch == 'd' || ch == 'D' || ch == 'f' || ch == 'F')) {
                return foundDigit
            }
            return if (ch == 'l'
                    || ch == 'L') {
                // not allowing L with an exponent
                foundDigit && !hasExp
            } else false
            // last character is illegal
        }
        // allowSigns is true iff the val ends in 'E'
        // found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
        return !allowSigns && foundDigit
    }
}