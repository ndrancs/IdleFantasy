package com.fantasyidler.simulator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [XpTable] — the level 1–99 XP progression table.
 *
 * Anchors are taken from the formula documented on [XpTable] and cross-checked
 * against the classic idle-RPG table (verified against IdleApes xp_table.json).
 */
class XpTableTest {

    @Test
    fun `known anchor thresholds match the classic table`() {
        assertEquals(0L, XpTable.xpForLevel(1))
        assertEquals(83L, XpTable.xpForLevel(2))
        assertEquals(101_333L, XpTable.xpForLevel(50))
        assertEquals(13_034_431L, XpTable.xpForLevel(99))
    }

    @Test
    fun `thresholds strictly increase across the whole range`() {
        for (level in 2..99) {
            assertTrue(
                "xpForLevel($level) must exceed xpForLevel(${level - 1})",
                XpTable.xpForLevel(level) > XpTable.xpForLevel(level - 1),
            )
        }
    }

    @Test
    fun `xpForLevel clamps out-of-range levels into 1-99`() {
        assertEquals(XpTable.xpForLevel(1), XpTable.xpForLevel(0))
        assertEquals(XpTable.xpForLevel(1), XpTable.xpForLevel(-5))
        assertEquals(XpTable.xpForLevel(99), XpTable.xpForLevel(200))
    }

    @Test
    fun `levelForXp is the inverse of xpForLevel at every threshold`() {
        for (level in 1..99) {
            assertEquals(level, XpTable.levelForXp(XpTable.xpForLevel(level)))
        }
    }

    @Test
    fun `levelForXp maps boundary XP values to the correct level`() {
        assertEquals(1, XpTable.levelForXp(0L))
        assertEquals(1, XpTable.levelForXp(82L))          // one short of level 2
        assertEquals(2, XpTable.levelForXp(83L))          // exactly level 2
        assertEquals(99, XpTable.levelForXp(13_034_431L))
        assertEquals(99, XpTable.levelForXp(Long.MAX_VALUE))
    }

    @Test
    fun `xpToNextLevel is the gap to the next threshold and zero at max level`() {
        assertEquals(83L, XpTable.xpToNextLevel(0L))
        assertEquals(0L, XpTable.xpToNextLevel(XpTable.xpForLevel(99)))
        assertEquals(0L, XpTable.xpToNextLevel(Long.MAX_VALUE))
    }

    @Test
    fun `nextLevelThreshold returns the upcoming threshold`() {
        assertEquals(83L, XpTable.nextLevelThreshold(0L))
        assertEquals(XpTable.xpForLevel(99), XpTable.nextLevelThreshold(XpTable.xpForLevel(99)))
    }

    @Test
    fun `progressFraction stays within 0 and 1 and is full at max level`() {
        assertEquals(0f, XpTable.progressFraction(0L), 0.0001f)
        assertEquals(1f, XpTable.progressFraction(XpTable.xpForLevel(99)), 0.0001f)
        // Halfway through the level-1 band (0..83) should be roughly 0.5.
        assertEquals(0.5f, XpTable.progressFraction(41L), 0.05f)
        for (xp in longArrayOf(0L, 50L, 1_000L, 101_333L, 5_000_000L, 13_034_431L)) {
            val f = XpTable.progressFraction(xp)
            assertTrue("progressFraction($xp)=$f out of range", f in 0f..1f)
        }
    }
}
