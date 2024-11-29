package sigma.dataAccess.model.data

import sigma.dataAccess.impl.data.CompletionStatus

interface IDay {
    fun getResults(): List<CompletionStatus>
}