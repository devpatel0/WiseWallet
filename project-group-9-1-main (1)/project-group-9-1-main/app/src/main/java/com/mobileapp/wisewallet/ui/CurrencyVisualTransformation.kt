package com.mobileapp.wisewallet.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import kotlin.math.min

/**
 * Composes a Currency `VisualTransformation` using the current theme.
 */
@Composable
fun currencyVisualTransformation(
    color: Color = MaterialTheme.colorScheme.onSurface,
    fillerColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
): CurrencyVisualTransformation {
    return CurrencyVisualTransformation(color, fillerColor)
}

/**
 * Provides a transformation from integer numerals into currency formatting.
 */
class CurrencyVisualTransformation internal constructor(
    private val color: Color,
    private val fillerColor: Color,
): VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val trans = transformText(text.text)

        val fakePrefix = when(text.text.length) {
            0 -> 4
            1 -> 3
            2 -> 2
            else -> 0
        }
        val ranges = mutableListOf<AnnotatedString.Range<SpanStyle>>().apply {
            if (fakePrefix > 0) {
                add(AnnotatedString.Range(SpanStyle(color = fillerColor), 0, fakePrefix))
            }
            if (text.text.isNotEmpty()) {
                add(AnnotatedString.Range(SpanStyle(color = color), fakePrefix, trans.length))
            }
        }

        return TransformedText(
            AnnotatedString(trans, spanStyles = ranges),
            CurrencyOffsetMapping(text.text.length, trans.length)
        )
    }


    /**
     * Provides bidirectional offset mapping between original and transformed currency text.
     */
    class CurrencyOffsetMapping(
        private val origLen: Int,
        private val transLen: Int
    ): OffsetMapping {

        override fun originalToTransformed(offset: Int): Int {
            return if (offset > origLen) {
                origLen
            } else {
                val rof = origLen - offset
                val trof = when {
                    rof < 3 -> rof
                    else -> rof + (rof / 3)
                }
                transLen - trof
            }
        }

        override fun transformedToOriginal(offset: Int): Int {
            return if (origLen < 3) {
                origLen - min(
                    transLen - offset,
                    origLen
                )
            } else {
                val trof = transLen - offset
                origLen - (trof - ((trof + 1) / 4))
            }
        }
    }

    companion object {

        /**
         * Formats the provided string as a US Currency (1,234.56).
         * This function does not prepend a currency symbol.
         *
         * Note: this function does not verify that the supplied characters
         *      are numeric, and will also format text.
         *
         * @param str a string representing the amount to be formatted, in cents.
         * @return a formatted string representing the provided quantity
         */
        fun transformText(str: String): String {
            val stack = str.padStart(3, '0').toCharArray().toMutableList()
            val sb = StringBuilder()
            var len = 0
            while (stack.isNotEmpty()) {
                val c = stack.removeLast()
                sb.append(c)
                len++
                if (len == 2) {
                 //   sb.append('.')
                } else if (len % 3 == 2) {
                    sb.append(',')
                }
            }
            return sb.toString().reversed()
                .removePrefix(",")
        }
    }
}

