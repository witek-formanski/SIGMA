package sigma.dataAccess.impl.parsers

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import sigma.dataAccess.impl.data.Configuration
import sigma.dataAccess.model.loggers.ILogger
import sigma.dataAccess.model.parsers.IConfigurationParser
import java.io.File

class JsonConfigurationParser(
    private var logger: ILogger
) : IConfigurationParser {
    override fun read(path: String): Configuration {
        val file = File(path)
        if (!file.exists()) {
            throw IllegalArgumentException("Configuration file not found: $path")
        }

        val json = file.readText()
        return try {
            Json.decodeFromString<Configuration>(json)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse configuration: ${e.message}", e)
        }
        logger.debug("Configuration read from file: $path.")
    }

    override fun write(path: String, configuration: Configuration) {
        File(path).writeText(Json.encodeToString(configuration))
        logger.debug("Configuration saved to file: $path.")
    }
}