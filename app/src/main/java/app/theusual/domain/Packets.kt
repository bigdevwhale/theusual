package app.theusual.domain

import app.theusual.data.Pattern

/**
 * A sugar packet sent to a friend: a café worth going to, with a line about what to order. It
 * travels as plain text through any messenger, readable without the app; a friend with The Usual
 * shares it back in, and it lands on their board as a dashed "want to go" packet.
 */
data class PacketCard(
    val name: String,
    val address: String = "",
    val city: String = "",
    val sub: String = "",
    val pattern: Pattern = Pattern.STRIPES,
    val palette: Int = 0,
    val note: String = "",
    val from: String = "",
)

object Packets {
    const val TAG = "#theusual"

    fun text(p: PacketCard): String = buildList {
        add("☕ " + p.name + if (p.sub.isNotBlank()) " — ${p.sub}" else "")
        listOf(p.address, p.city).filter { it.isNotBlank() }.joinToString(" · ").takeIf { it.isNotEmpty() }?.let(::add)
        if (p.note.isNotBlank()) add("«${p.note.trim()}»")
        if (p.from.isNotBlank()) add("— ${p.from.trim()}")
        add("$TAG ${p.pattern.name.lowercase()}/${p.palette}")
    }.joinToString("\n")

    /** Reads a packet back out of shared text; null when it isn't one. */
    fun parse(text: String): PacketCard? {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val tag = lines.indexOfFirst { it.startsWith(TAG) }
        if (tag < 0) return null
        val head = lines.firstOrNull { it.startsWith("☕") } ?: return null
        val title = head.removePrefix("☕").trim()
        val name = title.substringBefore(" — ").trim()
        if (name.isEmpty()) return null
        val sub = if (" — " in title) title.substringAfter(" — ").trim() else ""
        val headAt = lines.indexOf(head)
        val place = lines.getOrNull(headAt + 1)?.takeIf { !it.startsWith("«") && !it.startsWith("— ") && !it.startsWith(TAG) }
        val parts = place?.split(" · ").orEmpty()
        val note = lines.firstOrNull { it.startsWith("«") }?.removePrefix("«")?.removeSuffix("»")?.trim().orEmpty()
        val from = lines.firstOrNull { it.startsWith("— ") }?.removePrefix("— ")?.trim().orEmpty()
        val design = lines[tag].removePrefix(TAG).trim()
        val pattern = Pattern.of(design.substringBefore('/').uppercase())
        val palette = design.substringAfter('/', "0").trim().toIntOrNull() ?: 0
        return PacketCard(
            name = name,
            address = if (parts.size >= 2) parts.dropLast(1).joinToString(" · ") else parts.firstOrNull().orEmpty(),
            city = if (parts.size >= 2) parts.last() else "",
            sub = sub,
            pattern = pattern,
            palette = palette,
            note = note,
            from = from,
        )
    }
}
