package com.example.androidalarm

import java.util.Calendar

object HolidayChecker {

    fun isWeekend(calendar: Calendar): Boolean {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
    }

    fun isFederalHoliday(calendar: Calendar): Boolean {
        return isNewYearsDay(calendar) ||
                isMartinLutherKingJrDay(calendar) ||
                isPresidentsDay(calendar) ||
                isMemorialDay(calendar) ||
                isJuneteenth(calendar) ||
                isIndependenceDay(calendar) ||
                isLaborDay(calendar) ||
                isColumbusDay(calendar) ||
                isVeteransDay(calendar) ||
                isThanksgiving(calendar) ||
                isChristmasDay(calendar)
    }

    private fun isNewYearsDay(calendar: Calendar): Boolean {
        return isObservedOnWeekday(1, Calendar.JANUARY, calendar)
    }

    private fun isMartinLutherKingJrDay(calendar: Calendar): Boolean {
        return isNthMonday(3, Calendar.JANUARY, calendar)
    }

    private fun isPresidentsDay(calendar: Calendar): Boolean {
        return isNthMonday(3, Calendar.FEBRUARY, calendar)
    }

    private fun isMemorialDay(calendar: Calendar): Boolean {
        val month = calendar.get(Calendar.MONTH)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val totalDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        return month == Calendar.MAY &&
                dayOfWeek == Calendar.MONDAY &&
                (dayOfMonth + 7 > totalDays)
    }

    private fun isJuneteenth(calendar: Calendar): Boolean {
        return isObservedOnWeekday(19, Calendar.JUNE, calendar)
    }

    private fun isIndependenceDay(calendar: Calendar): Boolean {
        return isObservedOnWeekday(4, Calendar.JULY, calendar)
    }

    private fun isLaborDay(calendar: Calendar): Boolean {
        return isNthMonday(1, Calendar.SEPTEMBER, calendar)
    }

    private fun isColumbusDay(calendar: Calendar): Boolean {
        return isNthMonday(2, Calendar.OCTOBER, calendar)
    }

    private fun isVeteransDay(calendar: Calendar): Boolean {
        return isObservedOnWeekday(11, Calendar.NOVEMBER, calendar)
    }

    private fun isThanksgiving(calendar: Calendar): Boolean {
        val month = calendar.get(Calendar.MONTH)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val dayOfWeekInMonth = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH)
        return month == Calendar.NOVEMBER &&
                dayOfWeek == Calendar.THURSDAY &&
                dayOfWeekInMonth == 4
    }

    private fun isChristmasDay(calendar: Calendar): Boolean {
        return isObservedOnWeekday(25, Calendar.DECEMBER, calendar)
    }
    
    private fun isNthMonday(n: Int, month: Int, calendar: Calendar): Boolean {
        val calMonth = calendar.get(Calendar.MONTH)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val dayOfWeekInMonth = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH)
        return calMonth == month &&
                dayOfWeek == Calendar.MONDAY &&
                dayOfWeekInMonth == n
    }

    private fun isObservedOnWeekday(day: Int, month: Int, calendar: Calendar): Boolean {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, calendar.get(Calendar.YEAR))
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }

        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        var observedDay = day
        var observedMonth = month

        when (dayOfWeek) {
            Calendar.SATURDAY -> {
                cal.add(Calendar.DAY_OF_MONTH, -1)
                observedDay = cal.get(Calendar.DAY_OF_MONTH)
                observedMonth = cal.get(Calendar.MONTH)
            }
            Calendar.SUNDAY -> {
                cal.add(Calendar.DAY_OF_MONTH, 1)
                observedDay = cal.get(Calendar.DAY_OF_MONTH)
                observedMonth = cal.get(Calendar.MONTH)
            }
        }
        return calendar.get(Calendar.DAY_OF_MONTH) == observedDay &&
                calendar.get(Calendar.MONTH) == observedMonth
    }
}
