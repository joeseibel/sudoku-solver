package sudokusolver.kotlin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class BlockIndexTest {
    @Test
    fun testInit() {
        val rowUnder = assertThrows(IllegalArgumentException::class.java) { BlockIndex(-1, 0) }
        assertEquals("cellRow is -1, must be between 0 and 8.", rowUnder.message)

        val rowOver = assertThrows(IllegalArgumentException::class.java) { BlockIndex(9, 0) }
        assertEquals("cellRow is 9, must be between 0 and 8.", rowOver.message)

        val columnUnder = assertThrows(IllegalArgumentException::class.java) { BlockIndex(0, -1) }
        assertEquals("cellColumn is -1, must be between 0 and 8.", columnUnder.message)

        val columnOver = assertThrows(IllegalArgumentException::class.java) { BlockIndex(0, 9) }
        assertEquals("cellColumn is 9, must be between 0 and 8.", columnOver.message)

        BlockIndex(0, 0)
    }

    @Test
    fun getBlockRow() {
        assertEquals(0, BlockIndex(0, 0).blockRow)
        assertEquals(0, BlockIndex(1, 0).blockRow)
        assertEquals(0, BlockIndex(2, 0).blockRow)
        assertEquals(1, BlockIndex(3, 0).blockRow)
        assertEquals(1, BlockIndex(4, 0).blockRow)
        assertEquals(1, BlockIndex(5, 0).blockRow)
        assertEquals(2, BlockIndex(6, 0).blockRow)
        assertEquals(2, BlockIndex(7, 0).blockRow)
        assertEquals(2, BlockIndex(8, 0).blockRow)
    }

    @Test
    fun getBlockColumn() {
        assertEquals(0, BlockIndex(0, 0).blockColumn)
        assertEquals(0, BlockIndex(0, 1).blockColumn)
        assertEquals(0, BlockIndex(0, 2).blockColumn)
        assertEquals(1, BlockIndex(0, 3).blockColumn)
        assertEquals(1, BlockIndex(0, 4).blockColumn)
        assertEquals(1, BlockIndex(0, 5).blockColumn)
        assertEquals(2, BlockIndex(0, 6).blockColumn)
        assertEquals(2, BlockIndex(0, 7).blockColumn)
        assertEquals(2, BlockIndex(0, 8).blockColumn)
    }
}