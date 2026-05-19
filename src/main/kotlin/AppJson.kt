package net.lateinint

import kotlinx.serialization.json.Json

val AppJson: Json = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
    prettyPrint = false
}
