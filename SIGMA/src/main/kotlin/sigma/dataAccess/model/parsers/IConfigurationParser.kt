package sigma.dataAccess.model.parsers

import sigma.dataAccess.impl.data.Configuration
import java.io.File

interface IConfigurationParser {
    fun read(path: File) : Configuration
    fun write(path: File, configuration: Configuration) : Unit
}