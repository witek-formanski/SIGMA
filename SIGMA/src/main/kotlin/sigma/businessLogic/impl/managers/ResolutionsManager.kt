package sigma.businessLogic.impl.managers

import sigma.dataAccess.impl.data.CompletionStatus
import sigma.dataAccess.impl.data.Resolution
import sigma.dataAccess.impl.data.Timeline
import sigma.dataAccess.model.loggers.ILogger

class ResolutionsManager(
    private var resolutions: MutableList<Resolution>,
    private var timeline: Timeline,
    private var timeManager: TimeManager,
    private var logger: ILogger
) {
    fun addResolution(resolution: Resolution): Unit {
        // validate
        val name = resolution.getName()
        for (r in resolutions) {
            if (r.getName() == name) {
                logger.error("Cannot add resolution \"$name\". Resolution with this name already exists.")
                return
            }
        }

        // TODO("refactor")
        for (day in timeline.getDays()) {
            day.add(CompletionStatus.UNKNOWN)
        }

        resolutions.add(resolution)
        logger.debug("Resolution \"$name\" added successfully.")
    }

    fun removeResolution(name: String): Unit {
        val index = resolutions.indexOfFirst { it.getName() == name }
        if (index == -1) {
            logger.error("Cannot remove resolution \"$name\". Resolution with this name does not exist.")
            return
        }

        // TODO("refactor")
        for (day in timeline.getDays()) {
            day.removeAt(index)
        }

        resolutions.removeAt(index)
        logger.debug("Resolution \"$name\" removed successfully.")
    }

    fun modifyResolution(): Unit {
        TODO("enable changing name, description, and path")
    }

    fun moveResolution(from: Int, to: Int): Unit {
        if (from > resolutions.size || to > resolutions.size || from < 0 || to < 0 || from == to) {
            logger.error("Cannot move resolution to specified position.")
            logger.debug("Invalid indices: from = $from, to = $to.")
            return
        }

        // TODO("refactor")
        for (day in timeline.getDays()) {
            val status = day[from]
            day.removeAt(from)
            day.add(to, status)
        }

        val resolution = resolutions[from]
        resolutions.removeAt(from)
        resolutions.add(to, resolution)
        logger.debug("Resolution moved successfully from position $from to $to.")
    }
}