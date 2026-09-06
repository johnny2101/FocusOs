package com.example.focusos.domain.usecase

import android.content.pm.PackageManager
import com.example.focusos.model.AppUsageDiff
import com.example.focusos.util.getAppName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

class GenerateGitReportUseCase @Inject constructor(
    val packageManager: PackageManager
) {
    operator fun invoke(diffs: List<AppUsageDiff>, emergencyOverrides: Int = 0, authorName: String = "User"): String {
        val sb = StringBuilder()
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val totalCurrent = diffs.sumOf { it.currentWeekCount }
        val totalPrev = diffs.sumOf { it.previousWeekCount }
        val totalDiff = totalCurrent - totalPrev

        val status = if (totalDiff > 0) "REGRESSION DETECTED" else "OPTIMIZATION SUCCESS"

        sb.append("commit ${dateFormatter.format(Date())}\n")
        sb.append("Author: $authorName\n")
        sb.append("Status: $status\n\n")

        if (emergencyOverrides > 0) {
            sb.append("Emergency Override triggered: $emergencyOverrides\n\n")
        }

        sb.append("Changes:\n")

        if (diffs.isEmpty()) {
            sb.append(" No usage data recorded.\n")
            return sb.toString()
        }

        diffs.forEach { stat ->
            val sign = if (stat.diff > 0) "+" else if (stat.diff < 0) "-" else "="
            val label = when {
                stat.diff > 20 -> "(Major Regression)"
                stat.diff > 0 -> "(Minor Regression)"
                stat.diff < 0 -> "(Improvement)"
                else -> "(Stagnant)"
            }
            sb.append(String.format("%s %4d events %-25s %s\n", sign, abs(stat.diff), getAppName(stat.packageName, packageManager), label))


        }

        sb.append("\nTotal Delta: ${if (totalDiff > 0) "+" else ""}$totalDiff events\n")

        return sb.toString()
    }
}