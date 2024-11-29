package sigma.dataAccess.impl.data

import sigma.dataAccess.model.data.IDay

class Day : IDay {
    private var results : MutableList<CompletionStatus> = mutableListOf()

    fun getResults(): List<CompletionStatus> {
        return results.toList()
    }
}
