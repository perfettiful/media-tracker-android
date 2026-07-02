package edu.metrostate.ics342.mediatracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import edu.metrostate.ics342.mediatracker.theme.Finished
import edu.metrostate.ics342.mediatracker.theme.FinishedContainer
import edu.metrostate.ics342.mediatracker.theme.InProgress
import edu.metrostate.ics342.mediatracker.theme.InProgressContainer
import edu.metrostate.ics342.mediatracker.theme.WantTo
import edu.metrostate.ics342.mediatracker.theme.WantToContainer

// the want to / in progress / finished pill, container color behind status color text
@Composable
fun StatusBadge(
    status: LibraryStatus,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val (container, content) = when (status) {
        LibraryStatus.WANT_TO     -> WantToContainer to WantTo
        LibraryStatus.IN_PROGRESS -> InProgressContainer to InProgress
        LibraryStatus.FINISHED    -> FinishedContainer to Finished
    }
    Text(
        text  = stringResource(status.labelRes),
        style = MaterialTheme.typography.labelSmall,
        color = content,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(container)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
