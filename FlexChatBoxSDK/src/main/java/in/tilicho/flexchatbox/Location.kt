package `in`.tilicho.flexchatbox

data class Location(
    val location: android.location.Location ?= null,
    val url: String = ""
)
