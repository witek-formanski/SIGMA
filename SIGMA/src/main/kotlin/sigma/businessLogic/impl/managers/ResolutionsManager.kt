package sigma.businessLogic.impl.managers

import androidx.compose.ui.graphics.Color
import sigma.dataAccess.impl.data.*
import sigma.dataAccess.model.loggers.ILogger
import sigma.dataAccess.model.parsers.IConfigurationParser
import sigma.dataAccess.model.parsers.ITimelineParser
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// TODO("Refactor - move some responsibility to other classes???")
class ResolutionsManager(
    private val logger: ILogger,
    private val configurationParser: IConfigurationParser,
    private val timelineParser: ITimelineParser
) {
    private var configurationWritePath: File = File("appsettings.json")
    private var configurationReadPath: File = configurationWritePath
    private var configuration: Configuration = Configuration.getDefault()
    private var timeline: Timeline = Timeline.getDefault()

    fun tryInit(): Unit {
        configuration = configurationParser.read(configurationReadPath)
        logger.debug("Successfully parsed configuration.")
        timeline = timelineParser.read(configuration.timelinePath, configuration.resolutions.size)
        logger.debug("Successfully parsed timeline.")
    }

    fun close(): Unit {
        configurationParser.write(configurationWritePath, configuration)
        timelineParser.write(configuration.timelinePath, timeline)
    }

    fun setConfigurationReadPath(path: File): Unit {
        configurationReadPath = path
    }

    fun getResolutions(): List<Resolution> {
        return configuration.resolutions
    }

    fun addResolution(resolution: Resolution): Unit {
        // validate
        val name = resolution.name
        for (r in configuration.resolutions) {
            if (r.name == name) {
                logger.warn("Cannot add resolution \"$name\". Resolution with this name already exists.")
                return
            }
        }

        // TODO("refactor")
        for (day in timeline.days) {
            day.add(CompletionStatus.UNKNOWN)
        }

        configuration.resolutions.add(resolution)
        logger.debug("Resolution \"$name\" added successfully.")
    }

    fun removeResolution(name: String): Unit {
        // validate
        val index = configuration.resolutions.indexOfFirst { it.name == name }
        if (index == -1) {
            logger.error("Cannot remove resolution \"$name\". Resolution with this name does not exist.")
            return
        }

        // TODO("refactor")
        for (day in timeline.days) {
            day.removeAt(index)
            day.updateState()
        }

        configuration.resolutions.removeAt(index)
        logger.debug("Resolution \"$name\" removed successfully.")
    }

    fun modifyResolution(oldName: String, resolution: Resolution): Unit {
        // validate
        val index = configuration.resolutions.indexOfFirst { it.name == oldName }
        if (index == -1) {
            logger.error("Cannot modify resolution \"$oldName\". Resolution with this name does not exist.")
            return
        }

        configuration.resolutions[index] = resolution
        logger.debug("Resolution \"$oldName\" modified to \"${resolution.name}\" successfully.")
    }

    fun moveResolution(from: Int, to: Int): Unit {
        if (from > configuration.resolutions.size || to > configuration.resolutions.size || from < 0 || to < 0 || from == to) {
            logger.error("Cannot move resolution to specified position.")
            logger.debug("Invalid indices: from = $from, to = $to.")
            return
        }

        // TODO("refactor")
        for (day in timeline.days) {
            val status = day[from]
            day.removeAt(from)
            day.add(to, status)
        }

        val resolution = configuration.resolutions[from]
        configuration.resolutions.removeAt(from)
        configuration.resolutions.add(to, resolution)
        logger.debug("Resolution moved successfully from position $from to $to.")
    }

    fun getStartDate(): LocalDate {
        return timeline.startDate
    }

    fun isInRange(date: LocalDate): Boolean {
        return !date.isBefore(timeline.startDate) && !date.isAfter(LocalDate.now())
    }

    fun getDayColor(date: LocalDate): Color {
        val diff = ChronoUnit.DAYS.between(timeline.startDate, date)
        val state = getDayState(diff.toInt())
        return when (state) {
            DayState.RECORDED -> lerp(
                colorFromString(configuration.dayColors.success),
                colorFromString(configuration.dayColors.failure),
                getDaySuccessRate(timeline.days[diff.toInt()])
            )

            DayState.PAST -> colorFromString(configuration.dayColors.past)
            DayState.FUTURE -> colorFromString(configuration.dayColors.future)
            DayState.EMPTY -> colorFromString(configuration.dayColors.empty)
        }
    }

    private fun colorFromString(color: String): Color {
        if (color.length != 7 && color.length != 9) {
            val message = "Invalid color string: $color."
            logger.error(message)
            throw IllegalArgumentException(message)
        }

        val red = color.substring(1, 3).toInt(16) / 255.0f
        val green = color.substring(3, 5).toInt(16) / 255.0f
        val blue = color.substring(5, 7).toInt(16) / 255.0f
        if (color.length == 7) {
            return Color(red, green, blue)
        }
        val alpha = color.substring(7, 9).toInt(16) / 255.0f
        return Color(red, green, blue, alpha)
    }

    private fun lerp(start: Color, end: Color, fraction: Float): Color {
        val red = start.red * fraction + end.red * (1 - fraction)
        val green = start.green * fraction + end.green * (1 - fraction)
        val blue = start.blue * fraction + end.blue * (1 - fraction)
        val alpha = start.alpha * fraction + end.alpha * (1 - fraction)

        return Color(red, green, blue, alpha)
    }

    fun getDayState(diff: Int): DayState {
        return when {
            diff < 0 -> DayState.PAST
            diff >= timeline.days.size -> DayState.FUTURE
            else -> timeline.days[diff].getState()
        }
    }

    private fun getDaySuccessRate(day: Day): Float {
        if (day.getState() != DayState.RECORDED) {
            val message = "Cannot calculate success rate for day $day with state: ${day.getState()}."
            logger.error(message)
            throw IllegalArgumentException(message)
        }

        val statuses = day.getResults()
        val completed = statuses.count { it == CompletionStatus.COMPLETED }
        val partial = statuses.count { it == CompletionStatus.PARTIAL }
        val uncompleted = statuses.count { it == CompletionStatus.UNCOMPLETED }
        val unknown = statuses.count { it == CompletionStatus.UNKNOWN }
        val total = statuses.size
        val weights = configuration.completionStatusWeights
        val success = weights.completed * completed + weights.partial * partial +
                weights.uncompleted * uncompleted + weights.unknown * unknown
        return (success / total).toFloat()
    }

    fun getDay(date: LocalDate): Day {
        return timeline.days[getDiff(date)]
    }

    fun getScore(date: LocalDate): Double {
        val day = getDay(date)
        val statuses = day.getResults()
        val weights = configuration.completionStatusWeights

        val score = statuses.sumOf { status ->
            when (status) {
                CompletionStatus.COMPLETED -> weights.completed
                CompletionStatus.PARTIAL -> weights.partial
                CompletionStatus.UNCOMPLETED -> weights.uncompleted
                CompletionStatus.UNKNOWN -> weights.unknown
            }
        }

        return score / configuration.resolutions.size
    }

    fun getColorOfCompletionStatus(completionStatus: CompletionStatus): Color {
        return when (completionStatus) {
            CompletionStatus.COMPLETED -> colorFromString(configuration.dayColors.success)
            CompletionStatus.PARTIAL -> colorFromString(configuration.dayColors.partial)
            CompletionStatus.UNCOMPLETED -> colorFromString(configuration.dayColors.failure)
            CompletionStatus.UNKNOWN -> colorFromString(configuration.dayColors.empty)
        }
    }

    fun getCompletionStatus(date: LocalDate, index: Int): CompletionStatus {
        return getDay(date)[index]
    }

    fun getCountOf(completionStatus: CompletionStatus, date: LocalDate): Int {
        val day = getDay(date)
        return day.getResults().count { it == completionStatus }
    }

    fun getDiff(date: LocalDate): Int {
        val diff = ChronoUnit.DAYS.between(timeline.startDate, date).toInt()
        if (diff < 0 || diff >= timeline.days.size) {
            val message = "Date $date is out of timeline range."
            logger.error(message)
            throw IllegalArgumentException(message)
        }

        return diff
    }

    fun setCompletionStatus(date: LocalDate, resolutionName: String, status: CompletionStatus) {
        val day = getDay(date)
        val index = configuration.resolutions.indexOfFirst { it.name == resolutionName }
        if (index == -1) {
            val message =
                "Cannot set completion status for resolution \"$resolutionName\". Resolution with this name does not exist."
            logger.error(message)
            throw IllegalArgumentException(message)
        }

        day[index] = status
        day.updateState()
    }

    fun getMonthStatusesCounts(date: LocalDate): List<Int> {
        val startOfMonth = date.withDayOfMonth(1)
        val endOfMonth = date.withDayOfMonth(date.lengthOfMonth())
        val counts = mutableListOf(0, 0, 0, 0)

        for (day in startOfMonth.datesUntil(endOfMonth.plusDays(1))) {
            if (isInRange(day)) {
                val dayCounts = getDay(day).getStatusesCounts()
                for (i in counts.indices) {
                    counts[i] += dayCounts[i]
                }
            }
        }

        return counts
    }

    fun getMonthScore(date: LocalDate): Double {
        val startOfMonth = date.withDayOfMonth(1)
        val endOfMonth = date.withDayOfMonth(date.lengthOfMonth())
        var totalScore = 0.0
        var daysCount = 0

        for (day in startOfMonth.datesUntil(endOfMonth.plusDays(1))) {
            if (isInRange(day)) {
                totalScore += getScore(day)
                daysCount++
            }
        }

        return if (daysCount > 0) totalScore / daysCount else 0.0
    }
}