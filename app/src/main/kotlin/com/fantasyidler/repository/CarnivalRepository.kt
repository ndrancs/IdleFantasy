package com.fantasyidler.repository

import com.fantasyidler.data.json.CarnivalPrize
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarnivalRepository @Inject constructor(
    private val playerRepo: PlayerRepository,
    private val gameData: GameDataRepository,
    private val json: Json,
) {

    val prizes: Map<String, CarnivalPrize> by lazy {
        json.decodeFromString(gameData.loadAsset("data/carnival_prizes.json"))
    }

    suspend fun ticketBalance(): Int =
        playerRepo.getInventory()["carnival_ticket"] ?: 0

    /** Deduct [ticketCost] tickets and add [itemKey] to inventory (or pet list if it's a pet prize). Returns false if insufficient tickets. */
    suspend fun redeemForItem(itemKey: String, ticketCost: Int): Boolean =
        playerRepo.consumeItems(mapOf("carnival_ticket" to ticketCost)).also { success ->
            if (success) {
                val prize = prizes[itemKey]
                if (prize?.type == "pet") {
                    playerRepo.addPetIfNew(itemKey, gameData.pets[itemKey]?.boostPercent ?: 0)
                } else {
                    playerRepo.addItem(itemKey, 1)
                }
            }
        }

    /** Deduct [ticketCost] tickets and grant [xpAmount] XP in [skillKey]. Returns false if insufficient tickets. */
    suspend fun redeemForXp(skillKey: String, xpAmount: Long, ticketCost: Int): Boolean =
        playerRepo.consumeItems(mapOf("carnival_ticket" to ticketCost)).also { success ->
            if (success) playerRepo.applyMultiSkillResults(
                mapOf(skillKey to xpAmount),
                emptyMap(),
                0L,
            )
        }

    /** Award tickets directly (used by active minigames). */
    suspend fun awardTickets(count: Int) {
        if (count > 0) playerRepo.addItem("carnival_ticket", count)
    }
}
