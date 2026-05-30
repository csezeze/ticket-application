package com.turkcell.core.util

private val turkishMonthsShort = arrayOf(
    "Oca", "Sub", "Mar", "Nis", "May", "Haz", "Tem", "Agu", "Eyl", "Eki", "Kas", "Ara"
)

fun formatEventDate(isoText: String?): String {
    if (isoText.isNullOrBlank()) return "Tarih belirtilmedi"

    val datePart = isoText.substringBefore("T")
    val timePart = isoText.substringAfter("T", "").substringBefore("Z").take(5)
    val datePieces = datePart.split("-")

    if (datePieces.size != 3) {
        return isoText.replace("T", " ").replace("Z", "").take(16)
    }

    val year = datePieces[0]
    val month = datePieces[1].toIntOrNull()
    val day = datePieces[2].toIntOrNull()
    val monthText = month
        ?.takeIf { it in 1..12 }
        ?.let { turkishMonthsShort[it - 1] }

    if (day == null || monthText == null) {
        return isoText.replace("T", " ").replace("Z", "").take(16)
    }

    return buildString {
        append(day)
        append(" ")
        append(monthText)
        append(" ")
        append(year)
        if (timePart.length == 5) {
            append(", ")
            append(timePart)
        }
    }
}
