package com.axiel7.anihyou.ui.composables.scores

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.anihyou.R
import com.axiel7.anihyou.ui.theme.AniHyouTheme
import com.axiel7.anihyou.utils.NumberUtils.format
import com.axiel7.anihyou.utils.NumberUtils.toDoubleLocaleInvariant
import com.axiel7.anihyou.utils.NumberUtils.toDoubleOrNullLocaleInvariant
import com.axiel7.anihyou.utils.NumberUtils.toFloatOrNullLocaleInvariant

@Composable
fun SliderRatingView(
    maxValue: Double,
    modifier: Modifier = Modifier,
    initialRating: Double = 0.0,
    showAsDecimal: Boolean = false,
    onRatingChanged: (Double) -> Unit,
) {
    var ratingString by remember(initialRating) { mutableStateOf(initialRating.toString()) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = ratingString,
            onValueChange = { value ->
                if (value.isEmpty()) {
                    ratingString = value
                    onRatingChanged(0.0)
                } else {
                    value.toDoubleOrNullLocaleInvariant()?.let {
                        if (it == 0.0) {
                            ratingString = ""
                            onRatingChanged(0.0)
                        } else {
                            val valueRoundedString = it.format(
                                decimalLength = if (showAsDecimal) 1 else 0
                            )
                            val valueRounded = valueRoundedString.toDoubleLocaleInvariant()
                            if (valueRounded <= maxValue)
                                ratingString = valueRoundedString
                            onRatingChanged(valueRounded)
                        }
                    }
                }
            },
            modifier = Modifier.width(128.dp),
            label = { Text(text = stringResource(R.string.score)) },
            suffix = { Text(text = "/${maxValue.format(decimalLength = 0)}") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Slider(
            value = ratingString.toFloatOrNullLocaleInvariant() ?: 0f,
            onValueChange = {
                ratingString = if (it == 0f) "" else it.toDouble().format(
                    decimalLength = if (showAsDecimal) 1 else 0
                )
            },
            valueRange = 0f..maxValue.toFloat(),
            steps = if (maxValue <= 10.0 && !showAsDecimal) (maxValue.toInt() - 1) else 0,
            onValueChangeFinished = {
                onRatingChanged(ratingString.toDoubleOrNullLocaleInvariant() ?: 0.0)
            }
        )
    }
}

@Preview
@Composable
fun SliderRatingViewPreview() {
    AniHyouTheme {
        Surface {
            SliderRatingView(
                maxValue = 100.0,
                showAsDecimal = false,
                onRatingChanged = {}
            )
        }
    }
}