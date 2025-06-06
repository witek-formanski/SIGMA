package sigma.dataAccess.impl.data

class Day {
    private var state: DayState = DayState.EMPTY
    private var statuses: MutableList<CompletionStatus> = mutableListOf()

    fun getState(): DayState {
        return state
    }

    fun updateState(): Unit {
        for (status in statuses) {
            if (status != CompletionStatus.UNKNOWN) {
                state = DayState.RECORDED
                return
            }
            state = DayState.EMPTY
        }
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

    operator fun set(index: Int, completionStatus: CompletionStatus): Unit {
        statuses[index] = completionStatus
    }

    fun getStatusesCounts(): List<Int> {
        val counts = MutableList(CompletionStatus.entries.size) { 0 }
        for (status in statuses) {
            counts[status.ordinal]++
        }
        return counts
    }

    companion object {
        fun getEmpty(resolutionsCount: Int): Day {
            val day = Day()
            repeat(resolutionsCount) { day.add(CompletionStatus.UNKNOWN) }
            return day
        }
    }
}
