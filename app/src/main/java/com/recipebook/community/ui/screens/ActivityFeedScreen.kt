package com.recipebook.community.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipebook.community.data.model.ActivityLog
import com.recipebook.community.ui.components.AvatarView
import com.recipebook.community.ui.components.DoodleDivider
import com.recipebook.community.ui.theme.*
import com.recipebook.community.viewmodel.RecipeViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ActivityFeedScreen(recipeVM: RecipeViewModel) {
    val logs by recipeVM.activityLogs.collectAsState()

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Activity Feed", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        if (logs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No activity yet!", style = MaterialTheme.typography.headlineMedium)
            }
        } else {
            LazyColumn {
                items(logs) { log ->
                    ActivityItem(log)
                    DoodleDivider(Modifier.fillMaxWidth().height(12.dp).padding(start = 56.dp))
                }
            }
        }
    }
}

@Composable
fun ActivityItem(log: ActivityLog) {
    val actionColor = when (log.action) {
        "Approved" -> BoldTeal
        "Rejected" -> Coral
        "Deleted"  -> Coral
        "Added"    -> SoftPurple
        "Edited"   -> Amber
        else       -> BoldTeal
    }
    val actionEmoji = when (log.action) {
        "Approved" -> ""
        "Rejected" -> ""
        "Deleted"  -> ""
        "Added"    -> ""
        "Edited"   -> ""
        else       -> ""
    }
    Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AvatarView(log.userName, "#1D9E75", size = 36.dp)
        Column(Modifier.weight(1f)) {
            Text("${log.userName} $actionEmoji ${log.action} ",
                style = MaterialTheme.typography.bodyMedium)
            Text(log.recipeName, style = MaterialTheme.typography.labelLarge, color = actionColor)
            Text(timeAgo(log.timestamp), style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

fun timeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000    -> "just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else             -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
