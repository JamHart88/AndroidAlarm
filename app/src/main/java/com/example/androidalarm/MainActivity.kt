package com.example.androidalarm

import android.app.Activity
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidalarm.ui.theme.AndroidAlarmTheme
import kotlinx.coroutines.delay
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidAlarmTheme {
                AlarmClockScreen()
            }
        }
    }
}

@Composable
fun AlarmClockScreen(modifier: Modifier = Modifier) {
    var calendar by remember { mutableStateOf(Calendar.getInstance()) }
    var isAlarmOn by remember { mutableStateOf(false) }
    var isSettingAlarm by remember { mutableStateOf(false) }
    var isSettingBrightness by remember { mutableStateOf(false) }
    var isSelectingSound by remember { mutableStateOf(false) }
    var showHolidayConfirmation by remember { mutableStateOf(false) }

    var alarmHour by remember { mutableIntStateOf(7) }
    var alarmMinute by remember { mutableIntStateOf(0) }
    var settingStage by remember { mutableStateOf("hours") }
    var dayBrightness by remember { mutableFloatStateOf(1f) }
    var nightBrightness by remember { mutableFloatStateOf(0.5f) }
    var activeAlarmFile by remember { mutableStateOf("alarm_digital") }

    // Control the vertical offset of the time display from the center.
    // Positive values move it down, negative values move it up.
    val timeRowOffset by remember { mutableStateOf(0.dp) }

    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            calendar = Calendar.getInstance()
            delay(1000)
        }
    }

    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val isDayTime = currentHour in 7..22
    val brightnessLevel = if (isDayTime) dayBrightness else nightBrightness

    val activity = LocalContext.current as? Activity
    LaunchedEffect(brightnessLevel) {
        activity?.let { act ->
            act.window?.attributes = act.window.attributes.apply {
                screenBrightness = brightnessLevel
            }
        }
    }

    LaunchedEffect(calendar, isAlarmOn, alarmHour, alarmMinute, activeAlarmFile) {
        val isAlarmTime = calendar.get(Calendar.HOUR_OF_DAY) == alarmHour &&
                calendar.get(Calendar.MINUTE) == alarmMinute &&
                calendar.get(Calendar.SECOND) == 0

        if (isAlarmOn && isAlarmTime) {
            mediaPlayer?.release()
            val soundId = context.resources.getIdentifier(activeAlarmFile, "raw", context.packageName)
            mediaPlayer = MediaPlayer.create(context, soundId).apply {
                isLooping = true
                start()
            }
        } else if (!isAlarmOn) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopAppBar(
                calendar = calendar,
                isSettingAlarm = isSettingAlarm,
                isSettingBrightness = isSettingBrightness,
                onSetAlarmClick = {
                    isSettingAlarm = !isSettingAlarm
                    if (isSettingAlarm) {
                        isSettingBrightness = false
                        isSelectingSound = false
                        settingStage = "hours"
                    }
                },
                onBrightnessClick = {
                    isSettingBrightness = !isSettingBrightness
                    if (isSettingBrightness) {
                        isSettingAlarm = false
                        isSelectingSound = false
                    }
                },
                onSoundSettingsClick = {
                    isSelectingSound = !isSelectingSound
                    if (isSelectingSound) {
                        isSettingAlarm = false
                        isSettingBrightness = false
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = timeRowOffset)
        ) {
            if (isSettingAlarm) {
                AlarmTimeRow(
                    hour = alarmHour,
                    minute = alarmMinute,
                    settingStage = settingStage
                )
            } else {
                TimeRow(calendar = calendar)
            }
        }

        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            BottomControls(
                isSettingAlarm = isSettingAlarm,
                isSettingBrightness = isSettingBrightness,
                isAlarmOn = isAlarmOn,
                brightnessLevel = brightnessLevel,
                onAlarmClick = {
                    if (isAlarmOn) {
                        isAlarmOn = false
                    } else {
                        val now = Calendar.getInstance()
                        val alarmCalendar = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, alarmHour)
                            set(Calendar.MINUTE, alarmMinute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                            if (before(now)) {
                                add(Calendar.DAY_OF_MONTH, 1)
                            }
                        }
                        if (HolidayChecker.isWeekend(alarmCalendar) || HolidayChecker.isFederalHoliday(alarmCalendar)) {
                            showHolidayConfirmation = true
                        } else {
                            isAlarmOn = true
                        }
                    }
                },
                onBrightnessChange = {
                    if (isDayTime) {
                        dayBrightness = it
                    } else {
                        nightBrightness = it
                    }
                },
                onMinusClick = {
                    if (settingStage == "hours") {
                        alarmHour = (alarmHour - 1 + 24) % 24
                    } else {
                        alarmMinute = (alarmMinute - 1 + 60) % 60
                    }
                },
                onSetClick = {
                    if (settingStage == "hours") {
                        settingStage = "minutes"
                    } else {
                        isSettingAlarm = false
                    }
                },
                onPlusClick = {
                    if (settingStage == "hours") {
                        alarmHour = (alarmHour + 1) % 24
                    } else {
                        alarmMinute = (alarmMinute + 1) % 60
                    }
                }
            )
        }

        if (showHolidayConfirmation) {
            AlertDialog(
                onDismissRequest = { showHolidayConfirmation = false },
                title = { Text("Confirm Alarm") },
                text = { Text("The selected date is a weekend or a federal holiday. Do you want to set the alarm anyway?") },
                confirmButton = {
                    Button(onClick = {
                        isAlarmOn = true
                        showHolidayConfirmation = false
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    Button(onClick = { showHolidayConfirmation = false }) { Text("No") }
                }
            )
        }

        if (isSelectingSound) {
            SoundSelectionPopup(
                initialSound = activeAlarmFile,
                onSoundSelected = { 
                    activeAlarmFile = it
                    isSelectingSound = false
                 },
                onDismiss = { isSelectingSound = false }
            )
        }
    }
}

@Composable
private fun TopAppBar(
    calendar: Calendar,
    isSettingAlarm: Boolean,
    isSettingBrightness: Boolean,
    onSetAlarmClick: () -> Unit,
    onBrightnessClick: () -> Unit,
    onSoundSettingsClick: () -> Unit
) {
    val month = calendar.get(Calendar.MONTH)
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Image(painter = painterResource(id = R.drawable.settings), contentDescription = "Sound Settings", modifier = Modifier.width(120.dp).height(100.dp).clickable(onClick = onSoundSettingsClick))
            Image(painter = painterResource(id = if (isSettingAlarm) R.drawable.set_alarm else R.drawable.set_alarm_inactive), contentDescription = "Set Alarm", modifier = Modifier.width(120.dp).height(100.dp).clickable(onClick = onSetAlarmClick))
            Image(painter = painterResource(id = if (isSettingBrightness) R.drawable.brightness else R.drawable.brightness_off), contentDescription = "Brightness", modifier = Modifier.width(120.dp).height(100.dp).clickable(onClick = onBrightnessClick))
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(100.dp)) {
            Image(painter = painterResource(id = getMonthDrawable(month)), contentDescription = null)
            Image(painter = painterResource(id = getDigitDrawable(dayOfMonth / 10)), contentDescription = null, modifier = Modifier.height(60.dp))
            Image(painter = painterResource(id = getDigitDrawable(dayOfMonth % 10)), contentDescription = null, modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun SoundSelectionPopup(
    initialSound: String,
    onSoundSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val soundFiles = remember {
        listOf("alarm_birds", "alarm_classic", "alarm_digital", "alarm_fututistic", "alarm_retro", "alarm_rooster", "alarm_short", "alarm_vintage")
    }
    var selectedSound by remember { mutableStateOf(initialSound) }
    val context = LocalContext.current
    var previewPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    fun playSound(sound: String, loopCount: Int) {
        previewPlayer?.release()
        val soundId = context.resources.getIdentifier(sound, "raw", context.packageName)
        previewPlayer = MediaPlayer.create(context, soundId).apply {
            var count = 1
            setOnCompletionListener {
                if (count < loopCount) {
                    it.start()
                    count++
                } else {
                    it.release()
                    previewPlayer = null
                }
            }
            start()
        }
    }

    DisposableEffect(Unit) {
        onDispose { previewPlayer?.release() }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.width(300.dp).background(Color.DarkGray).padding(16.dp)) {
            Text("Select Alarm Sound", color = Color.White, modifier = Modifier.padding(bottom = 16.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(soundFiles) { soundFile ->
                    Text(
                        text = soundFile.replace("_", " ").replaceFirstChar { it.titlecase() },
                        color = if (soundFile == selectedSound) Color.Yellow else Color.White,
                        modifier = Modifier.fillMaxWidth().clickable { 
                            selectedSound = soundFile
                            playSound(soundFile, 3)
                        }.padding(8.dp)
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.End) {
                Button(onClick = onDismiss) { Text("Cancel") }
                Button(onClick = { onSoundSelected(selectedSound) }, modifier = Modifier.padding(start = 8.dp)) { Text("OK") }
            }
        }
    }
}

@Composable
private fun BottomControls(
    isSettingAlarm: Boolean,
    isSettingBrightness: Boolean,
    isAlarmOn: Boolean,
    brightnessLevel: Float,
    onAlarmClick: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onMinusClick: () -> Unit,
    onSetClick: () -> Unit,
    onPlusClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
        if (isSettingAlarm) {
            Row(modifier = Modifier.fillMaxWidth().offset(y = 70.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = R.drawable.minus), contentDescription = "Minus", modifier = Modifier.width(150.dp).height(150.dp).clickable(onClick = onMinusClick))
                Image(painter = painterResource(id = R.drawable.set), contentDescription = "Set", modifier = Modifier.width(150.dp).height(150.dp).clickable(onClick = onSetClick))
                Image(painter = painterResource(id = R.drawable.plus), contentDescription = "Plus", modifier = Modifier.width(150.dp).height(150.dp).clickable(onClick = onPlusClick))
            }
        } else if (isSettingBrightness) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 24.dp).offset(y = 40.dp)){
                Image(painter = painterResource(id = R.drawable.brightness_off), contentDescription = null, modifier = Modifier.height(40.dp).padding(end = 16.dp))
                Slider(value = brightnessLevel, onValueChange = onBrightnessChange, modifier = Modifier.weight(1f))
                Image(painter = painterResource(id = R.drawable.brightness), contentDescription = null, modifier = Modifier.height(40.dp).padding(start = 16.dp))
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 50.dp).offset(y=30.dp)) {
                Image(painter = painterResource(id = if (isAlarmOn) R.drawable.alarm else R.drawable.alarm_off), contentDescription = if (isAlarmOn) "Alarm On" else "Alarm Off", modifier = Modifier.width(145.dp).height(120.dp).clickable(onClick = onAlarmClick))
            }
        }
    }
}

@Composable
private fun AlarmTimeRow(modifier: Modifier = Modifier, hour: Int, minute: Int, settingStage: String) {
    var blinkOn by remember { mutableStateOf(true) }
    LaunchedEffect(settingStage) {
        if (settingStage == "hours" || settingStage == "minutes") {
            while (true) {
                blinkOn = !blinkOn
                delay(500)
            }
        }
    }

    val hourAlpha = if (settingStage == "hours" && !blinkOn) 0.3f else 1f
    val minuteAlpha = if (settingStage == "minutes" && !blinkOn) 0.3f else 1f

    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    val displayHour = if (cal.get(Calendar.HOUR) == 0) 12 else cal.get(Calendar.HOUR)
    val isAm = cal.get(Calendar.AM_PM) == Calendar.AM

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Image(painter = painterResource(id = getDigitDrawable(displayHour / 10)), contentDescription = null, modifier = Modifier.padding(start = 10.dp).height(260.dp), alpha = hourAlpha)
        Image(painter = painterResource(id = getDigitDrawable(displayHour % 10)), contentDescription = null, modifier = Modifier.padding(start = 10.dp).height(260.dp), alpha = hourAlpha)
        Image(painter = painterResource(id = R.drawable.colon), contentDescription = null, modifier = Modifier.padding(start = 10.dp).height(100.dp))
        Image(painter = painterResource(id = getDigitDrawable(minute / 10)), contentDescription = null, modifier = Modifier.padding(start = 10.dp).height(260.dp), alpha = minuteAlpha)
        Image(painter = painterResource(id = getDigitDrawable(minute % 10)), contentDescription = null, modifier = Modifier.padding(start = 10.dp).height(260.dp), alpha = minuteAlpha)
        Image(painter = painterResource(id = if (isAm) R.drawable.am else R.drawable.pm), contentDescription = null, modifier = Modifier.padding(start = 10.dp).height(70.dp))
    }
}

@Composable
private fun TimeRow(modifier: Modifier = Modifier, calendar: Calendar) {
    var showColon by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            showColon = !showColon
            delay(500)
        }
    }

    val hour = calendar.get(Calendar.HOUR)
    val minute = calendar.get(Calendar.MINUTE)
    val isAm = calendar.get(Calendar.AM_PM) == Calendar.AM
    val displayHour = if (hour == 0) 12 else hour

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Image(painter = painterResource(id = getDigitDrawable(displayHour / 10)), contentDescription = null, modifier = Modifier.padding(start = 10.dp).height(260.dp))
        Image(painter = painterResource(id = getDigitDrawable(displayHour % 10)), contentDescription = null, modifier = Modifier.padding(start = 10.dp).height(260.dp))
        Image(painter = painterResource(id = R.drawable.colon), contentDescription = null, modifier = Modifier.padding(start = 10.dp).height(100.dp), alpha = if (showColon) 1f else 0f)
        Image(painter = painterResource(id = getDigitDrawable(minute / 10)), contentDescription = null, modifier = Modifier.padding(start = 10.dp).height(260.dp))
        Image(painter = painterResource(id = getDigitDrawable(minute % 10)), contentDescription = null, modifier = Modifier.padding(start = 10.dp).height(260.dp))
        Image(painter = painterResource(id = if (isAm) R.drawable.am else R.drawable.pm), contentDescription = null, modifier = Modifier.padding(start = 10.dp).height(70.dp))
    }
}

fun getDigitDrawable(digit: Int): Int {
    return when (digit) {
        0 -> R.drawable.zero
        1 -> R.drawable.one
        2 -> R.drawable.two
        3 -> R.drawable.three
        4 -> R.drawable.four
        5 -> R.drawable.five
        6 -> R.drawable.six
        7 -> R.drawable.seven
        8 -> R.drawable.eight
        9 -> R.drawable.nine
        else -> R.drawable.zero
    }
}

fun getMonthDrawable(month: Int): Int {
    return when (month) {
        Calendar.JANUARY -> R.drawable.jan
        Calendar.FEBRUARY -> R.drawable.feb
        Calendar.MARCH -> R.drawable.mar
        Calendar.APRIL -> R.drawable.apr
        Calendar.MAY -> R.drawable.may
        Calendar.JUNE -> R.drawable.jun
        Calendar.JULY -> R.drawable.jul
        Calendar.AUGUST -> R.drawable.aug
        Calendar.SEPTEMBER -> R.drawable.sep
        Calendar.OCTOBER -> R.drawable.oct
        Calendar.NOVEMBER -> R.drawable.nov
        Calendar.DECEMBER -> R.drawable.dec
        else -> R.drawable.jan
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape")
@Composable
fun AlarmClockScreenPreview() {
    AndroidAlarmTheme {
        AlarmClockScreen()
    }
}
