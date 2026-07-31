package edu.metrostate.ics342.mediatracker.ui.priorities

import edu.metrostate.ics342.mediatracker.data.MediaRepository
import edu.metrostate.ics342.mediatracker.data.model.Priority
import edu.metrostate.ics342.mediatracker.data.model.UpdatePriorityRequest
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrioritiesViewModelTest {

    private val repo = mockk<MediaRepository>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun priority(mediaId: Int, orderIndex: Int) = Priority(
        mediaId    = mediaId,
        priority   = 1,
        orderIndex = orderIndex,
    )

    private val threeItems = listOf(priority(1, 0), priority(2, 1), priority(3, 2))

    @Test
    fun `moving an item puts it in the new spot`() {
        coEvery { repo.getPriorities() } returns threeItems
        val viewModel = PrioritiesViewModel(repo)

        viewModel.moveItem(0, 2)

        assertEquals(listOf(2, 3, 1), viewModel.priorities.value.map { it.mediaId })
    }

    @Test
    fun `dropping renumbers orderIndex top to bottom and saves the ones that moved`() {
        coEvery { repo.getPriorities() } returns threeItems
        coEvery { repo.setPriority(any()) } returns true
        val viewModel = PrioritiesViewModel(repo)

        viewModel.beginDrag()
        viewModel.moveItem(0, 2)
        viewModel.saveOrder()

        assertEquals(listOf(0, 1, 2), viewModel.priorities.value.map { it.orderIndex })
        // every position changed here so all three get pushed
        coVerify(exactly = 1) { repo.setPriority(UpdatePriorityRequest(2, 1, 0, null, null)) }
        coVerify(exactly = 1) { repo.setPriority(UpdatePriorityRequest(3, 1, 1, null, null)) }
        coVerify(exactly = 1) { repo.setPriority(UpdatePriorityRequest(1, 1, 2, null, null)) }
    }

    @Test
    fun `a failed save puts the old order back`() {
        coEvery { repo.getPriorities() } returns threeItems
        coEvery { repo.setPriority(any()) } returns false
        val viewModel = PrioritiesViewModel(repo)

        viewModel.beginDrag()
        viewModel.moveItem(0, 2)
        viewModel.saveOrder()

        assertEquals(listOf(1, 2, 3), viewModel.priorities.value.map { it.mediaId })
        assertNotNull(viewModel.actionError.value)
    }

    @Test
    fun `dropping an item back where it started doesnt call the api`() {
        coEvery { repo.getPriorities() } returns threeItems
        coEvery { repo.setPriority(any()) } returns true
        val viewModel = PrioritiesViewModel(repo)

        viewModel.beginDrag()
        viewModel.moveItem(0, 1)
        viewModel.moveItem(1, 0)
        viewModel.saveOrder()

        coVerify(exactly = 0) { repo.setPriority(any()) }
    }
}
