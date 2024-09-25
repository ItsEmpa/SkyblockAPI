package tech.thatgravyboat.skyblockapi.api.events.info

import tech.thatgravyboat.skyblockapi.api.events.base.SkyblockEvent

/**
 * Fired when the scoreboard changes in SkyBlock.
 */
data class ScoreboardChangeEvent(val old: List<String>, val new: List<String>) : SkyblockEvent() {

    val added: List<String> = new - old.toSet()
}
