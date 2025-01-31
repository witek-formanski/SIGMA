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
    override fun read(file: File): Configuration {
        if (!file.exists()) {
            val message = "Configuration file not found: ${file.path}."
            logger.error(message)
            throw IllegalArgumentException(message)
        }

        val json = file.readText()
        val configuration = try {
            Json.decodeFromString<Configuration>(json)
        } catch (e: Exception) {
            val message = "Failed to parse configuration: ${e.message}."
            logger.error(message)
            throw IllegalArgumentException(message, e)
        }
        logger.debug("Configuration read from file: ${file.path}.")
        return configuration
    }

    override fun write(file: File, configuration: Configuration): Unit {
        try {
            file.writeText(Json.encodeToString(configuration))
        } catch (e: Exception) {
            val message = "Failed to save configuration to file: ${file.path}."
            logger.error(message)
            throw IllegalArgumentException(message, e)
        }
        logger.debug("Configuration saved to file: ${file.path}.")
    }
}