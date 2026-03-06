package com.donotnotify.donotnotify.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.AccessAlarms
import androidx.compose.material.icons.automirrored.outlined.Rule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.donotnotify.donotnotify.BlockerRule
import com.donotnotify.donotnotify.RuleType
import com.donotnotify.donotnotify.ui.components.EmptyState

@Composable
fun RulesScreen(
    rules: List<BlockerRule>,
    onRuleClick: (BlockerRule) -> Unit,
    onDeleteRuleClick: (BlockerRule) -> Unit, // This lambda is no longer directly used for UI, but kept for consistency if needed elsewhere.
    onBrowsePrebuiltRulesClick: () -> Unit
) {
    var showAddRuleDialog by remember { mutableStateOf(false) }

    if (showAddRuleDialog) {
        AlertDialog(
            onDismissRequest = { showAddRuleDialog = false },
            title = { Text("How to Add a New Rule") },
            text = {
                Text(
                    "To create a new rule:\n\n" +
                    "1. Switch to the \"History\" tab\n" +
                    "2. Find a notification you want to block\n" +
                    "3. Tap on the notification\n" +
                    "4. Select \"Create Rule\" from the options\n" +
                    "5. Customize the rule settings as needed\n\n" +
                    "The rule will then appear here and automatically block matching notifications."
                )
            },
            confirmButton = {
                TextButton(onClick = { showAddRuleDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }

    // Group rules by packageName; apps with multiple rules get a section header
    val grouped = rules.groupBy { it.packageName ?: it.appName ?: "" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = WindowInsets.navigationBars.asPaddingValues()
    ) {
        if (rules.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.AutoMirrored.Outlined.Rule,
                    title = "No Rules Created",
                    description = "Create rules to automatically block unwanted notifications. Tap a notification in the History tab to create a rule, or browse pre-built rules.",
                    actionLabel = "Browse Pre-built Rules",
                    onAction = onBrowsePrebuiltRulesClick
                )
            }
        } else {
            item {
                Text(
                    text = "Any notifications matching the rules given below will be blocked automatically.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }
            grouped.forEach { (_, appRules) ->
                if (appRules.size == 1) {
                    // Single rule — show as flat card (unchanged)
                    item(key = "rule_${appRules[0].packageName}_${appRules[0].ruleType}_0") {
                        RuleCard(rule = appRules[0], showAppName = true, onClick = { onRuleClick(appRules[0]) })
                    }
                } else {
                    // Multiple rules — show a section header then indented rule cards
                    val appName = appRules[0].appName.orEmpty()
                    item(key = "header_${appRules[0].packageName}") {
                        Text(
                            text = "$appName (${appRules.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp, bottom = 4.dp)
                        )
                        HorizontalDivider()
                    }
                    itemsIndexed(
                        appRules,
                        key = { index, rule -> "rule_${rule.packageName}_${rule.ruleType}_$index" }
                    ) { _, rule ->
                        RuleCard(
                            rule = rule,
                            showAppName = false,
                            modifier = Modifier.padding(start = 8.dp),
                            onClick = { onRuleClick(rule) }
                        )
                    }
                }
            }
        }
        item {
            Button(
                onClick = { showAddRuleDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Add a New Rule")
            }
            Button(
                onClick = onBrowsePrebuiltRulesClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp)
            ) {
                Text("Browse Pre-built Rules")
            }
        }
    }
}

@Composable
private fun RuleCard(
    rule: BlockerRule,
    showAppName: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .alpha(if (rule.isEnabled) 1f else 0.5f)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp)
            ) {
                if (showAppName) {
                    Text(
                        text = rule.appName.orEmpty(),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (rule.isEnabled) null else TextDecoration.LineThrough
                    )
                }
                val titleFilterText = if (rule.titleFilter.isNullOrBlank()) "N/A" else rule.titleFilter.orEmpty()
                Text(
                    text = "Title: $titleFilterText",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val textFilterText = if (rule.textFilter.isNullOrBlank()) "N/A" else rule.textFilter.orEmpty()
                Text(
                    text = "Text: $textFilterText",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (rule.advancedConfig?.isTimeLimitEnabled == true) {
                Icon(
                    imageVector = Icons.Filled.AccessAlarms,
                    contentDescription = "Time limited rule",
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                val icon = if (rule.ruleType == RuleType.DENYLIST) Icons.Filled.Block else Icons.Filled.CheckCircle
                Icon(
                    imageVector = icon,
                    contentDescription = rule.ruleType.name
                )
                if (rule.hitCount > 0) {
                    Text(
                        text = "Hits: ${rule.hitCount}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "No hits",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
