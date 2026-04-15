package com.phoneclaw.ai.ui.chat

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phoneclaw.ai.ui.theme.*

enum class PromptTemplateType { FREE_FORM, REWRITE_TONE, SUMMARIZE_TEXT, CODE_SNIPPET }

data class PromptTemplate(
    val type: PromptTemplateType,
    val label: String,
    val options: List<String>,
    val optionLabel: String,
    val promptBuilder: (option: String, input: String) -> String,
)

val PROMPT_TEMPLATES = listOf(
    PromptTemplate(
        type = PromptTemplateType.FREE_FORM,
        label = "Free Form",
        options = emptyList(),
        optionLabel = "",
        promptBuilder = { _, input -> input },
    ),
    PromptTemplate(
        type = PromptTemplateType.REWRITE_TONE,
        label = "Rewrite Tone",
        options = listOf("Formal", "Casual", "Friendly", "Polite", "Enthusiastic", "Concise"),
        optionLabel = "Tone",
        promptBuilder = { option, input -> "Rewrite the following text using a $option tone:\n\n$input" },
    ),
    PromptTemplate(
        type = PromptTemplateType.SUMMARIZE_TEXT,
        label = "Summarize",
        options = listOf("Key bullet points (3-5)", "Short paragraph (1-2 sentences)", "Concise summary (~50 words)", "Headline/title", "One-sentence summary"),
        optionLabel = "Style",
        promptBuilder = { option, input -> "Please summarize the following in $option:\n\n$input" },
    ),
    PromptTemplate(
        type = PromptTemplateType.CODE_SNIPPET,
        label = "Code Snippet",
        options = listOf("C++", "Java", "JavaScript", "Kotlin", "Python", "Swift", "TypeScript"),
        optionLabel = "Language",
        promptBuilder = { option, input -> "Write a $option code snippet to $input" },
    ),
)

@Composable
fun PromptLabTemplateBar(
    selectedType: PromptTemplateType,
    onSelect: (PromptTemplateType) -> Unit,
) {
    ScrollableTabRow(
        selectedTabIndex = PROMPT_TEMPLATES.indexOfFirst { it.type == selectedType },
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = AccentViolet,
        edgePadding = 8.dp,
    ) {
        PROMPT_TEMPLATES.forEach { template ->
            Tab(
                selected = selectedType == template.type,
                onClick = { onSelect(template.type) },
                text = {
                    Text(
                        text = template.label,
                        fontSize = 13.sp,
                        fontWeight = if (selectedType == template.type) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
            )
        }
    }
}

@Composable
fun PromptLabOptionsRow(
    template: PromptTemplate,
    selectedOption: String,
    onOptionChange: (String) -> Unit,
) {
    if (template.options.isEmpty()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        template.options.forEach { option ->
            FilterChip(
                selected = selectedOption == option,
                onClick = { onOptionChange(option) },
                label = { Text(option, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentViolet,
                    selectedLabelColor = ForegroundInverse,
                ),
            )
        }
    }
}

fun buildPrompt(template: PromptTemplate, selectedOption: String, userInput: String): String {
    val option = if (template.options.isEmpty()) "" else selectedOption.ifEmpty { template.options.firstOrNull() ?: "" }
    return template.promptBuilder(option, userInput)
}
