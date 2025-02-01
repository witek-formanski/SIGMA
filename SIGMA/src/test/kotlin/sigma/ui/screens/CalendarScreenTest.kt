package sigma.ui.screens

import androidx.compose.ui.test.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import sigma.businessLogic.impl.managers.ResolutionsManager
import sigma.dataAccess.impl.data.Configuration
import sigma.dataAccess.impl.data.Timeline
import sigma.dataAccess.model.loggers.ILogger
import sigma.dataAccess.model.parsers.IConfigurationParser
import sigma.dataAccess.model.parsers.ITimelineParser
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

class CalendarScreenTest {

    private lateinit var logger: ILogger
    private lateinit var configurationParser: IConfigurationParser
    private lateinit var timelineParser: ITimelineParser
    private lateinit var resolutionsManager: ResolutionsManager

    @BeforeEach
    fun setUp() {
        logger = Mockito.mock(ILogger::class.java)
        configurationParser = Mockito.mock(IConfigurationParser::class.java)
        timelineParser = Mockito.mock(ITimelineParser::class.java)

        `when`(configurationParser.read(any())).thenReturn(Configuration.getDefault())
        `when`(timelineParser.read(any(), any())).thenReturn(Timeline.getDefault())

        resolutionsManager = ResolutionsManager(logger, configurationParser, timelineParser)
        resolutionsManager.tryInit() // Initialize before each test
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `test CalendarScreen displays given initDate correctly`() = runComposeUiTest {
        val testDate = GregorianCalendar(2025, Calendar.JANUARY, 15).time
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        val expectedDateString = dateFormat.format(testDate)

        setContent {
            CalendarScreen(manager = resolutionsManager).Calendar(
                onDateSelected = {},
                onBackClick = {},
                initDate = testDate
            )
        }

        onNodeWithText(expectedDateString).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `test CalendarScreen allows month navigation`() = runComposeUiTest {
        val startDate = GregorianCalendar(2024, Calendar.MARCH, 1).time

        setContent {
            CalendarScreen(manager = resolutionsManager).Calendar(
                onDateSelected = {},
                onBackClick = {},
                initDate = startDate
            )
        }

        onNodeWithContentDescription("Next Month").performClick()
        onNodeWithText("APRIL").assertIsDisplayed()
        onNodeWithContentDescription("Previous Month").performClick()
        onNodeWithText("MARCH").assertIsDisplayed()
        onNodeWithContentDescription("Next Month").performClick()
        onNodeWithText("APRIL").assertIsDisplayed()
        onNodeWithContentDescription("Next Month").performClick()
        onNodeWithText("MAY").assertIsDisplayed()
        repeat(3) { onNodeWithContentDescription("Next Month").performClick() }
        onNodeWithText("AUGUST").assertIsDisplayed()
        repeat(5) { onNodeWithContentDescription("Next Month").performClick() }
        onNodeWithText("JANUARY").assertIsDisplayed()
    }
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `test CalendarScreen displays initial date correctly`() = runComposeUiTest {
        val testDate = GregorianCalendar(2025, Calendar.JANUARY, 15).time
        val expectedDateString = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(testDate)

        setContent {
            CalendarScreen(manager = resolutionsManager).Calendar(
                onDateSelected = {},
                onBackClick = {},
                initDate = testDate
            )
        }

        onNodeWithText(expectedDateString).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `test CalendarScreen allows year navigation`() = runComposeUiTest {
        val startDate = GregorianCalendar(2024, Calendar.JUNE, 1).time

        setContent {
            CalendarScreen(manager = resolutionsManager).Calendar(
                onDateSelected = {},
                onBackClick = {},
                initDate = startDate
            )
        }

        onNodeWithText("2024").performClick()
        onNodeWithText("2025").performClick()
        onNodeWithText("2025").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `test CalendarScreen does not allow selecting out-of-range dates`() = runComposeUiTest {
        val minYear = 2020
        val maxYear = 2025

        setContent {
            CalendarScreen(manager = resolutionsManager).Calendar(
                onDateSelected = {},
                onBackClick = {},
                minYear = minYear,
                maxYear = maxYear
            )
        }

        onNodeWithText("2025").assertIsDisplayed()
        onNodeWithText("2019").assertDoesNotExist()
        onNodeWithText("2026").assertDoesNotExist()
    }
}
