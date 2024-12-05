package sigma.dataAccess.impl.data

import java.time.LocalDate

class Timeline(
    private val startDate: LocalDate
) {
    private var days: MutableList<Day> = mutableListOf()

    fun getDays(): MutableList<Day> {
        return days
    }

    fun getStartDate(): LocalDate {
        return startDate
    }
}