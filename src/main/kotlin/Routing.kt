package tech.gloucestercounty

import com.beust.klaxon.Klaxon
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import tech.gloucestercounty.schedules.imsaSchedule
import kotlin.collections.sortedBy

var imsa = imsaSchedule()
var combined = listOf<Map<String, Any>>()
    get() {
        field = listOf()
        field += imsa.values
        field = field.sortedBy {
            return@sortedBy it["startDate"] as String
        }
        return field
    }

fun Application.configureRouting() {
    routing {
        get("/db") {
            call.respond(Klaxon().toJsonString(combined))
        }

        get("/imsa") {
            imsa = imsaSchedule()
            call.respond(Klaxon().toJsonString(combined))
        }
    }
}
