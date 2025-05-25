package sigma.dataAccess.impl.data

enum class CompletionStatus(private val code: String) {
    UNCOMPLETED("0"),
    COMPLETED("1"),
    PARTIAL("2"),
    UNKNOWN("3");

    override fun toString(): String {
        return code
    }

    companion object {
        fun fromString(code: String?): CompletionStatus {
            return entries.find { it.code == code } ?: UNKNOWN
        }
    }
}
