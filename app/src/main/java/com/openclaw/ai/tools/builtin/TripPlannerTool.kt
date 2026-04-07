package com.openclaw.ai.tools.builtin

import com.openclaw.ai.data.model.ToolDefinition
import com.openclaw.ai.data.model.ToolInvocation
import com.openclaw.ai.data.model.ToolParameter
import com.openclaw.ai.data.model.ToolResult
import com.openclaw.ai.tools.ToolExecutor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripPlannerTool @Inject constructor() : ToolExecutor {

    override val definition = ToolDefinition(
        name = "trip_planner",
        description = "Create a day-by-day trip itinerary for a given destination and duration.",
        parameters = listOf(
            ToolParameter(
                name = "destination",
                type = "string",
                description = "The travel destination (e.g., 'Paris, France').",
                required = true,
            ),
            ToolParameter(
                name = "days",
                type = "string",
                description = "Number of days for the trip (e.g., '3').",
                required = true,
            ),
            ToolParameter(
                name = "interests",
                type = "string",
                description = "Optional comma-separated interests to tailor the itinerary (e.g., 'history, food, art').",
                required = false,
            ),
        ),
    )

    override suspend fun execute(invocation: ToolInvocation): ToolResult {
        val destination = invocation.parameters["destination"]?.toString()?.trim()
        if (destination.isNullOrBlank()) {
            return ToolResult(
                toolName = invocation.toolName,
                output = "Missing required parameter: 'destination'.",
                isError = true,
            )
        }

        val days = invocation.parameters["days"]?.toString()?.trim()?.toIntOrNull()
        if (days == null || days <= 0) {
            return ToolResult(
                toolName = invocation.toolName,
                output = "Parameter 'days' must be a positive integer.",
                isError = true,
            )
        }

        val interests = invocation.parameters["interests"]?.toString()
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()

        val plan = buildItinerary(destination, days, interests)
        return ToolResult(toolName = invocation.toolName, output = plan)
    }

    private fun buildItinerary(destination: String, days: Int, interests: List<String>): String {
        val interestNote = if (interests.isNotEmpty()) {
            " tailored for: _${interests.joinToString(", ")}_"
        } else {
            ""
        }

        val sb = StringBuilder()
        sb.appendLine("# $days-Day Trip to $destination$interestNote")
        sb.appendLine()
        sb.appendLine("> _This itinerary is a starting point. Adjust timings based on your pace and local conditions._")
        sb.appendLine()

        val templates = getDayTemplates(destination, interests)

        for (day in 1..days) {
            sb.appendLine("## Day $day")
            sb.appendLine()

            val template = templates[(day - 1) % templates.size]

            sb.appendLine("**Morning**")
            sb.appendLine("- ${template.morning}")
            sb.appendLine()

            sb.appendLine("**Afternoon**")
            sb.appendLine("- ${template.afternoon}")
            sb.appendLine()

            sb.appendLine("**Evening**")
            sb.appendLine("- ${template.evening}")
            sb.appendLine()
        }

        sb.appendLine("---")
        sb.appendLine("**Travel Tips for $destination**")
        sb.appendLine("- Book accommodations and popular attractions in advance.")
        sb.appendLine("- Check local transportation options (metro, bus, taxi).")
        sb.appendLine("- Carry local currency and a backup payment method.")
        if (interests.isNotEmpty()) {
            sb.appendLine("- Look for guided tours focused on: ${interests.joinToString(", ")}.")
        }

        return sb.toString().trimEnd()
    }

    private data class DayTemplate(val morning: String, val afternoon: String, val evening: String)

    private fun getDayTemplates(destination: String, interests: List<String>): List<DayTemplate> {
        val hasHistory = interests.any { it.contains("history", ignoreCase = true) }
        val hasFood = interests.any { it.contains("food", ignoreCase = true) || it.contains("culinary", ignoreCase = true) }
        val hasArt = interests.any { it.contains("art", ignoreCase = true) || it.contains("culture", ignoreCase = true) }
        val hasNature = interests.any { it.contains("nature", ignoreCase = true) || it.contains("outdoor", ignoreCase = true) }

        return listOf(
            DayTemplate(
                morning = if (hasHistory) "Visit the main historical landmark or old town district of $destination."
                          else "Arrive and check in. Take a leisurely walk around your neighborhood.",
                afternoon = if (hasFood) "Explore the local market or food hall. Try signature dishes of $destination."
                            else "Visit the top-rated attraction in $destination.",
                evening = "Dinner at a well-reviewed local restaurant. Evening stroll through the city center.",
            ),
            DayTemplate(
                morning = if (hasArt) "Morning at the city's main art museum or gallery."
                          else "Explore a popular neighborhood or district.",
                afternoon = if (hasNature) "Head to a nearby park, garden, or natural attraction."
                            else "Afternoon guided tour or self-guided walk of key sights.",
                evening = if (hasFood) "Food tour or cooking class to learn about local cuisine."
                          else "Live music, cultural show, or evening at a local bar.",
            ),
            DayTemplate(
                morning = "Day trip to a nearby attraction or neighboring town.",
                afternoon = "Return and visit any sights you missed. Browse local shops.",
                evening = "Farewell dinner. Reflect on your favorite moments in $destination.",
            ),
        )
    }
}
