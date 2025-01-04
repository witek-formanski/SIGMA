package sigma.dataAccess.impl.data

class Day(private val state: DayState) {
    private var statuses: MutableList<CompletionStatus> = mutableListOf()

    fun getState(): DayState {
        return state
    }

    fun getResults(): List<CompletionStatus> {
        return statuses.toList()
    }

    fun add(completionStatus: CompletionStatus) {
        statuses.add(completionStatus)
    }

    fun add(index: Int, completionStatus: CompletionStatus) {
        statuses.add(index, completionStatus)
    }

    fun removeAt(index: Int) {
        statuses.removeAt(index)
    }

    operator fun get(index: Int): CompletionStatus {
        return statuses[index]
    }

    companion object {
        fun getEmpty(resolutionsCount: Int): Day {
            val day = Day(DayState.EMPTY)
            repeat(resolutionsCount) { day.add(CompletionStatus.UNKNOWN) }
            return day
        }
    }
}
