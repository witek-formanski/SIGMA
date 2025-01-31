import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import sigma.businessLogic.impl.managers.ResolutionsManager
import sigma.dataAccess.impl.data.*
import sigma.dataAccess.model.loggers.ILogger
import sigma.dataAccess.model.parsers.IConfigurationParser
import sigma.dataAccess.model.parsers.ITimelineParser
import java.io.File
import kotlin.test.assertEquals

class ResolutionsManagerTest {

    private lateinit var logger: ILogger
    private lateinit var configurationParser: IConfigurationParser
    private lateinit var timelineParser: ITimelineParser
    private lateinit var resolutionsManager: ResolutionsManager

    @BeforeEach
    fun setUp() {
        logger = mock(ILogger::class.java)
        configurationParser = mock(IConfigurationParser::class.java)
        timelineParser = mock(ITimelineParser::class.java)

        // Ensure getDefault returns a mutable configuration
        `when`(configurationParser.read(any(File::class.java)))
            .thenReturn(Configuration.getDefault().copy(resolutions = mutableListOf()))

        resolutionsManager = ResolutionsManager(logger, configurationParser, timelineParser)
        resolutionsManager.tryInit() // Initialize before each test
    }

    @Test
    fun `test addResolution with new resolution`() {
        val resolution = Resolution("New Resolution")

        resolutionsManager.addResolution(resolution)

        assertEquals(1, resolutionsManager.getResolutions().size)
        assertEquals("New Resolution", resolutionsManager.getResolutions()[0].name)
        verify(logger).debug("Resolution \"New Resolution\" added successfully.")
    }

    @Test
    fun `test addResolution with existing resolution`() {
        val resolution = Resolution("Existing Resolution")
        resolutionsManager.addResolution(resolution) // Add first
        resolutionsManager.addResolution(resolution) // Try adding duplicate

        assertEquals(1, resolutionsManager.getResolutions().size)
        verify(logger).warn("Cannot add resolution \"Existing Resolution\". Resolution with this name already exists.")
    }

    @Test
    fun `test removeResolution with existing resolution`() {
        val resolution = Resolution("Existing Resolution")
        resolutionsManager.addResolution(resolution)

        resolutionsManager.removeResolution("Existing Resolution")

        assertEquals(0, resolutionsManager.getResolutions().size)
        verify(logger).debug("Resolution \"Existing Resolution\" removed successfully.")
    }

    @Test
    fun `test removeResolution with non-existing resolution`() {
        resolutionsManager.removeResolution("Non-Existing Resolution")

        assertEquals(0, resolutionsManager.getResolutions().size)
        verify(logger).error("Cannot remove resolution \"Non-Existing Resolution\". Resolution with this name does not exist.")
    }

    @Test
    fun `test modifyResolution with existing resolution`() {
        val oldResolution = Resolution("Old Resolution")
        val newResolution = Resolution("New Resolution")
        resolutionsManager.addResolution(oldResolution)

        resolutionsManager.modifyResolution("Old Resolution", newResolution)

        assertEquals(1, resolutionsManager.getResolutions().size)
        assertEquals("New Resolution", resolutionsManager.getResolutions()[0].name)
        verify(logger).debug("Resolution \"Old Resolution\" modified to \"New Resolution\" successfully.")
    }

    @Test
    fun `test modifyResolution with non-existing resolution`() {
        val newResolution = Resolution("New Resolution")

        resolutionsManager.modifyResolution("Non-Existing Resolution", newResolution)

        assertEquals(0, resolutionsManager.getResolutions().size)
        verify(logger).error("Cannot modify resolution \"Non-Existing Resolution\". Resolution with this name does not exist.")
    }

    @Test
    fun `test moveResolution with valid indices`() {
        val resolution1 = Resolution("Resolution 1")
        val resolution2 = Resolution("Resolution 2")
        resolutionsManager.addResolution(resolution1)
        resolutionsManager.addResolution(resolution2)

        resolutionsManager.moveResolution(0, 1)

        assertEquals("Resolution 2", resolutionsManager.getResolutions()[0].name)
        assertEquals("Resolution 1", resolutionsManager.getResolutions()[1].name)
        verify(logger).debug("Resolution moved successfully from position 0 to 1.")
    }

    @Test
    fun `test moveResolution with invalid indices`() {
        val resolution1 = Resolution("Resolution 1")
        resolutionsManager.addResolution(resolution1)

        resolutionsManager.moveResolution(0, 2) // Out of bounds

        assertEquals(1, resolutionsManager.getResolutions().size) // Should not change
        verify(logger).error("Cannot move resolution to specified position.")
    }
}
