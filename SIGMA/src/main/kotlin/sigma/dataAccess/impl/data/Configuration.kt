package sigma.dataAccess.impl.data

import kotlinx.serialization.Serializable

@Serializable
data class Configuration(
    var resolutions: MutableList<Resolution>,
    val timelinePath: String,
    val completionStatusWeights: CompletionStatusWeights,
    val dayColors: DayColors
) {
    companion object {
        fun getDefault(): Configuration {
            return Configuration(
                mutableListOf(),
                "timeline.csv",
                CompletionStatusWeights(1.0, 0.5, 0.0, 0.0),
                DayColors("#4caf50","#ffc107","#f44336","#ffffff","#9e9e9e","#9e9e9e"))
                // green, amber, red, grey, white, white
        }
    }

    fun update(resolutions: MutableList<Resolution>) {
        this.resolutions = resolutions
    }
}

@Serializable
data class CompletionStatusWeights(
    val completed: Double,
    val partial: Double,
    val uncompleted: Double,
    val unknown: Double
)

@Serializable
data class DayColors(
    val success: String,
    val partial: String,
    val failure: String,
    val empty: String,
    val future: String,
    val past: String
)
