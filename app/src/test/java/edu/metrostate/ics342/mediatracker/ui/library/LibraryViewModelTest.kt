package edu.metrostate.ics342.mediatracker.ui.library

import edu.metrostate.ics342.mediatracker.data.LibraryResult
import edu.metrostate.ics342.mediatracker.data.MediaRepository
import edu.metrostate.ics342.mediatracker.data.model.LibraryItem
import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import edu.metrostate.ics342.mediatracker.data.model.Media
import edu.metrostate.ics342.mediatracker.data.model.PriorityLevel
import edu.metrostate.ics342.mediatracker.R
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    private val repo = mockk<MediaRepository>()

    @Before
    fun setUp() {
        // viewModelScope needs a main dispatcher, unconfined so launches run inline
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun dune(id: Int) = LibraryItem(
        userId    = "user-1",
        mediaId   = id,
        status    = LibraryStatus.WANT_TO,
        addedAt   = "2026-07-23T00:00:00Z",
        updatedAt = "2026-07-23T00:00:00Z",
        media     = Media(id = id, mediaType = "movie", title = "Dune"),
    )

    private fun priority(mediaId: Int) = edu.metrostate.ics342.mediatracker.data.model.Priority(
        mediaId    = mediaId,
        priority   = 1,
        orderIndex = 0,
    )

    @Test
    fun `a sixth priority never reaches the api`() {
        coEvery { repo.getLibrary(any()) } returns LibraryResult.Success(listOf(dune(1)))
        // already at the cap with five other things
        coEvery { repo.getPriorities() } returns (10..14).map { priority(it) }
        val viewModel = LibraryViewModel(repo)

        viewModel.setPriority(1, PriorityLevel.HIGH, 3, "nope")

        coVerify(exactly = 0) { repo.setPriority(any()) }
        assertEquals(R.string.priorities_full, viewModel.actionError.value)
    }

    @Test
    fun `editing one thats already on the list still works at the cap`() {
        coEvery { repo.getLibrary(any()) } returns LibraryResult.Success(listOf(dune(1)))
        coEvery { repo.getPriorities() } returns listOf(priority(1)) + (10..13).map { priority(it) }
        coEvery { repo.setPriority(any()) } returns true
        val viewModel = LibraryViewModel(repo)

        viewModel.setPriority(1, PriorityLevel.HIGH, 3, "still fine")

        coVerify(exactly = 1) { repo.setPriority(any()) }
    }

    @Test
    fun `remove takes the item out of the list right away`() {
        coEvery { repo.getLibrary(any()) } returns LibraryResult.Success(listOf(dune(1)))
        coEvery { repo.removeFromLibrary(1) } returns true
        val viewModel = LibraryViewModel(repo)

        viewModel.removeItem(1)

        assertTrue(viewModel.libraryItems.value.isEmpty())
    }

    @Test
    fun `failed remove rolls the item back`() {
        coEvery { repo.getLibrary(any()) } returns LibraryResult.Success(listOf(dune(1)))
        // the repo eats the IOException and hands back false, thats the failure signal
        coEvery { repo.removeFromLibrary(1) } returns false
        val viewModel = LibraryViewModel(repo)

        viewModel.removeItem(1)

        assertEquals(listOf(1), viewModel.libraryItems.value.map { it.mediaId })
        assertNotNull(viewModel.actionError.value)
    }
}
