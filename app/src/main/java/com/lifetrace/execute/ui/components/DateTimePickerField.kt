package com.lifetrace.execute.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.lifetrace.execute.ui.theme.LifeMuted
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun DateTimePickerField(
    label: String,
    value: String?,
    onValueChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val zoneId = ZoneId.systemDefault()
    val parsed = value?.let { raw ->
        runCatching { Instant.parse(raw).atZone(zoneId) }.getOrNull()
    }

    Column(modifier = modifier) {
        Text(label, color = LifeMuted)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = {
                    val seed = parsed ?: ZonedDateTime.now(zoneId)
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    val selected = LocalDateTime.of(
                                        year,
                                        month + 1,
                                        day,
                                        hour,
                                        minute,
                                    ).atZone(zoneId).toInstant().toString()
                                    onValueChange(selected)
                                },
                                seed.hour,
                                seed.minute,
                                true,
                            ).show()
                        },
                        seed.year,
                        seed.monthValue - 1,
                        seed.dayOfMonth,
                    ).show()
                },
            ) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                Text(
                    parsed?.format(DISPLAY_FORMAT) ?: "选择日期和时间",
                )
            }
            if (value != null) {
                TextButton(onClick = { onValueChange(null) }) {
                    Text("清除")
                }
            }
        }
    }
}

private val DISPLAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
