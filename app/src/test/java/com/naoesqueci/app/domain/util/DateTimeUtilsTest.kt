package com.naoesqueci.app.domain.util

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/**
 * Regressão do bug "a volta volta um dia": o DatePicker devolve meia-noite
 * em UTC; em fusos negativos o dia civil local é o anterior.
 */
class DateTimeUtilsTest {

    private val defaultZone: TimeZone = TimeZone.getDefault()

    @Before
    fun setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(defaultZone)
    }

    private fun utcMidnight(year: Int, month: Int, day: Int): Long {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun localDay(millis: Long): Int {
        return Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.DAY_OF_MONTH)
    }

    private fun localTime(millis: Long): Pair<Int, Int> {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return cal.get(Calendar.HOUR_OF_DAY) to cal.get(Calendar.MINUTE)
    }

    @Test
    fun `combineDateTime puro reproduz o bug - dia volta um em UTC-3`() {
        val pickerMillis = utcMidnight(2026, Calendar.SEPTEMBER, 20)
        val timeMillis = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 30)
        }.timeInMillis

        val buggy = DateTimeUtils.combineDateTime(pickerMillis, timeMillis)

        // Documenta o bug original: 20/set UTC vira 19/set local.
        assertThat(localDay(buggy)).isEqualTo(19)
    }

    @Test
    fun `pickerDateToLocal preserva o dia civil escolhido`() {
        val pickerMillis = utcMidnight(2026, Calendar.SEPTEMBER, 20)
        val timeMillis = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 30)
        }.timeInMillis

        val result = DateTimeUtils.pickerDateToLocal(pickerMillis, timeMillis)

        assertThat(localDay(result)).isEqualTo(20)
        assertThat(localTime(result)).isEqualTo(10 to 30)
    }

    @Test
    fun `pickerDateToLocal preserva horario de borda como 23h59`() {
        val pickerMillis = utcMidnight(2026, Calendar.SEPTEMBER, 20)
        val timeMillis = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
        }.timeInMillis

        val result = DateTimeUtils.pickerDateToLocal(pickerMillis, timeMillis)

        assertThat(localDay(result)).isEqualTo(20)
        assertThat(localTime(result)).isEqualTo(23 to 59)
    }

    @Test
    fun `localToPickerMillis e pickerDateToLocal fazem round-trip`() {
        val original = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 20, 14, 45, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val picker = DateTimeUtils.localToPickerMillis(original)
        val back = DateTimeUtils.pickerDateToLocal(picker, original)

        assertThat(DateTimeUtils.formatDate(back)).isEqualTo(DateTimeUtils.formatDate(original))
        assertThat(DateTimeUtils.formatTime(back)).isEqualTo(DateTimeUtils.formatTime(original))
    }
}
