package com.example.androidalarm

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.androidalarm.ui.theme.AndroidAlarmTheme

@Composable
fun SetAlarmScreen(modifier: Modifier = Modifier, navController: NavController) {
    var alarmHour by remember { mutableStateOf(12) }
    var alarmMinute by remember { mutableStateOf(0) }
    var isAm by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Hour
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.plus),
                        contentDescription = "Increase Hour",
                        modifier = Modifier.clickable { alarmHour = (alarmHour + 1) % 24 }
                    )
                    Row {
                        Image(painter = painterResource(id = getDigitDrawable(alarmHour / 10)), contentDescription = null)
                        Image(painter = painterResource(id = getDigitDrawable(alarmHour % 10)), contentDescription = null)
                    }
                    Image(
                        painter = painterResource(id = R.drawable.minus),
                        contentDescription = "Decrease Hour",
                        modifier = Modifier.clickable { alarmHour = (alarmHour - 1 + 24) % 24 }
                    )
                }
                // Minute
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.plus),
                        contentDescription = "Increase Minute",
                        modifier = Modifier.clickable { alarmMinute = (alarmMinute + 1) % 60 }
                    )
                    Row {
                        Image(painter = painterResource(id = getDigitDrawable(alarmMinute / 10)), contentDescription = null)
                        Image(painter = painterResource(id = getDigitDrawable(alarmMinute % 10)), contentDescription = null)
                    }
                    Image(
                        painter = painterResource(id = R.drawable.minus),
                        contentDescription = "Decrease Minute",
                        modifier = Modifier.clickable { alarmMinute = (alarmMinute - 1 + 60) % 60 }
                    )
                }
                // AM/PM
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = if (isAm) R.drawable.am else R.drawable.pm),
                        contentDescription = "AM/PM",
                        modifier = Modifier.clickable { isAm = !isAm }
                    )
                }
            }
        }

        Image(
            painter = painterResource(id = R.drawable.button),
            contentDescription = "Back",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .clickable { navController.popBackStack() }
        )
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape")
@Composable
fun SetAlarmScreenPreview() {
    AndroidAlarmTheme {
        SetAlarmScreen(navController = rememberNavController())
    }
}
