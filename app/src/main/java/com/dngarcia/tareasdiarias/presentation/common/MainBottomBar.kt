package com.dngarcia.tareasdiarias.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dngarcia.tareasdiarias.R

enum class MainBottomDestination {
    TODAY,
    TOMORROW,
    TOP_TEN,
    TASKS,
    MENU,
}

@Composable
fun MainBottomBar(
    selectedDestination: MainBottomDestination,
    onOpenToday: () -> Unit,
    onOpenTomorrow: () -> Unit,
    onOpenTopTen: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MainBottomBarItem(
                iconRes = R.drawable.ic_today_home_photo,
                label = stringResource(id = R.string.today_title),
                selected = selectedDestination == MainBottomDestination.TODAY,
                onClick = onOpenToday,
                preserveOriginalColors = true,
            )
            MainBottomBarItem(
                iconRes = R.drawable.ic_tomorrow_tab_calendar,
                label = stringResource(id = R.string.tomorrow_title),
                selected = selectedDestination == MainBottomDestination.TOMORROW,
                onClick = onOpenTomorrow,
                preserveOriginalColors = true,
            )
            MainBottomBarItem(
                iconRes = R.drawable.ic_top10_tab_calendar,
                label = stringResource(id = R.string.top10_tab_label),
                selected = selectedDestination == MainBottomDestination.TOP_TEN,
                onClick = onOpenTopTen,
                preserveOriginalColors = true,
            )
            MainBottomBarItem(
                iconRes = R.drawable.ic_today_tasks_photo,
                label = stringResource(id = R.string.tasks_title),
                selected = selectedDestination == MainBottomDestination.TASKS,
                onClick = onOpenTasks,
                preserveOriginalColors = true,
            )
            MainBottomBarItem(
                iconRes = R.drawable.ic_today_menu,
                label = stringResource(id = R.string.today_menu),
                selected = selectedDestination == MainBottomDestination.MENU,
                onClick = onOpenMenu,
                preserveOriginalColors = true,
            )
        }
    }
}

@Composable
private fun RowScope.MainBottomBarItem(
    iconRes: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    preserveOriginalColors: Boolean = false,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = if (preserveOriginalColors) {
                Color.Unspecified
            } else if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}
