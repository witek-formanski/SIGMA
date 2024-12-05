package sigma.dataAccess.impl.parsers

import sigma.dataAccess.impl.data.Resolution
import sigma.dataAccess.impl.data.Timeline
import sigma.dataAccess.impl.data.CompletionStatus
import sigma.dataAccess.impl.data.Day
import sigma.dataAccess.impl.managers.TimeManager
import sigma.dataAccess.model.parsers.IParser
import java.io.File

class CsvParser : IParser {
    override fun readResolutions(path: String): MutableList<Resolution> {
        val file = File(path)
        if (!file.exists()) {
            throw IllegalArgumentException("Resolutions file not found: $path")
        }

        val lines = file.readLines()
        if (lines.size < 3) {
            throw IllegalArgumentException("Invalid resolutions file format.")
        }

        val names = lines[0].split(",")
        val descriptions = lines[1].split(",")
        val images = lines[2].split(",")

        if (names.size != descriptions.size || names.size != images.size) {
            throw IllegalArgumentException("Mismatch in counts of names, descriptions, and images.")
        }

        val resolutions = mutableListOf<Resolution>()
        for (i in names.indices) {
            val name = names[i].takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException("Resolution name cannot be blank.")
            val description = descriptions[i].takeIf { it.isNotBlank() }
            val image = images[i].takeIf { it.isNotBlank() }
            resolutions.add(Resolution(name, description, image))
        }

        return resolutions
    }

    override fun readTimeline(path: String): Timeline {
        val file = File(path)
        if (!file.exists()) {
            throw IllegalArgumentException("Timeline file not found: $path")
        }

        val lines = file.readLines()
        if (lines.isEmpty()) {
            throw IllegalArgumentException("Timeline file is empty.")
        }

        val dateResolutionStatuses = lines.map { line ->
            val tokens = line.split(",")
            val date = TimeManager.parse(tokens[0])
            val statuses = tokens.drop(1).map { CompletionStatus.fromString(it) }
            date to statuses
        }

        val resolutionsCount = dateResolutionStatuses.first().second.size
        if (dateResolutionStatuses.any { it.second.size != resolutionsCount }) {
            throw IllegalArgumentException("All days must have the same number of statuses as the first day.")
        }

        val startDate = dateResolutionStatuses.first().first
        val timeline = Timeline(startDate)
        var currentDate = startDate

        for ((date, statuses) in dateResolutionStatuses) {
            while (currentDate.isBefore(date)) {
                timeline.getDays().add(Day.createEmptyDay(resolutionsCount))
                currentDate = currentDate.plusDays(1)
            }
            val day = Day()
            statuses.forEach { day.add(it) }
            timeline.getDays().add(day)
            currentDate = currentDate.plusDays(1)
        }

        val today = TimeManager.today()
        while (currentDate.isBefore(today)) {
            timeline.getDays().add(Day.createEmptyDay(resolutionsCount))
            currentDate = currentDate.plusDays(1)
        }

        return timeline
    }

    override fun writeResolutions(path: String, resolutions: List<Resolution>) {
        val names = resolutions.joinToString(",") { it.name }
        val descriptions = resolutions.joinToString(",") { it.description ?: "" }
        val images = resolutions.joinToString(",") { it.image ?: "" }
        File(path).writeText("$names\n$descriptions\n$images\n")
    }

    override fun writeTimeline(path: String, timeline: Timeline) {
        val lines = timeline.getDays().mapIndexed { index, day ->
            val date = TimeManager.toString(timeline.getStartDate().plusDays(index.toLong()))
            val statuses = day.getResults().joinToString(",") { it.toString() }
            "$date,$statuses"
        }
        File(path).writeText(lines.joinToString("\n"))
    }
}
