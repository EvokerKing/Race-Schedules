package tech.gloucestercounty.schedules

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Locale

val firstFormat: DateTimeFormatter = DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("MMM d").parseDefaulting(ChronoField.YEAR, 2026).toFormatter(Locale.ENGLISH)
val secondFormat: DateTimeFormatter = DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("EEEE, MMMM d, y").toFormatter(Locale.ENGLISH)
val thirdFormat: DateTimeFormatter = DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("h:mm a").toFormatter(Locale.ENGLISH)

fun imsaSchedule(): Map<String, Map<String, Any>> {
    val races = mutableMapOf<String, Map<String, Any>>()

    for (link in listOf("https://www.imsa.com/weathertech/weathertech-2026-schedule/", "https://www.imsa.com/michelinpilotchallenge/imsa-michelin-pilot-challenge-2026-schedule/", "https://www.imsa.com/vpracingsportscarchallenge/imsa-vp-racing-sportscar-challenge-2026-schedule/")) {
        val wtRes = Jsoup.connect(link).get()
        for (i in wtRes.select("div.schedule-item").toList()) {
            val raceLink = i.select("div.schedule-ctas > button > a")[0].attributes()["href"]
            val raceRes = Jsoup.connect(raceLink).get()
            val scheduleItems = raceRes.select("div.day-event-header, div.day-event-details-container")
            val days = mutableListOf<MutableList<Element>>()

            scheduleItems.forEach {
                if (it.hasClass("day-event-header")) {
                    days.add(mutableListOf(it))
                } else {
                    days.lastOrNull()?.add(it)
                }
            }
            races += raceLink to mapOf(
                "title" to i.select("h2.schedule-title")[0].text(),
                "series" to "imsa",
                "link" to raceLink,
                "startDate" to LocalDate.parse(i.select("div.schedule-date")[0].text().split(" - ")[0], firstFormat)
                    .toString(),
                "endDate" to LocalDate.parse(i.select("div.schedule-date")[0].text().split(" - ")[1], firstFormat)
                    .toString(),
                "track" to i.select("div.schedule-location")[0].text(),
                "schedule" to days.map { j ->
                    LocalDate.parse(j.removeFirst().text(), secondFormat).toString() to j.map { k ->
                        listOf(
                            k.select("div.event-name")[0].text(),
                            LocalTime.parse(k.select("div.event-time")[0].text().split(" to ")[0], thirdFormat).toString(),
                            LocalTime.parse(k.select("div.event-time")[0].text().split(" to ")[1].replace(Regex("M .+"), "M"), thirdFormat).toString())
                    }
                }
            )
        }
    }

    return races
}