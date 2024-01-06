class State(private val n: Int, private val m: Int) {
    companion object {
        const val PLAYERS = 2
        const val initialFencesCount = 10
    }

    data class Cell(val x: Int, val y: Int) {
        operator fun plus(delta: Delta) = Cell(x + delta.x, y + delta.y)
        operator fun minus(delta: Delta) = Cell(x - delta.x, y - delta.y)
        operator fun minus(other: Cell) = Delta(x - other.x, y - other.y)
    }

    data class Delta(val x: Int, val y: Int) {
        operator fun times(k: Int) = Delta(k * x, k * y)
    }

    private val players = arrayOf(Cell(m / 2, 0), Cell(m / 2, n - 1))
    private val playersPreview: Array<Cell?> = Array(PLAYERS) { null }

    private val freeFences = IntArray(PLAYERS) { initialFencesCount }

    var current = 0

    enum class FenceType {
        ABSENT, HORIZONTAL, VERTICAL, HORIZONTAL_PREVIEW, VERTICAL_PREVIEW;

        fun isPut() = this == HORIZONTAL || this == VERTICAL

        fun isPreview() = this == HORIZONTAL_PREVIEW || this == VERTICAL_PREVIEW

        fun withoutPreview() = when (this) {
            ABSENT, HORIZONTAL, VERTICAL -> this
            HORIZONTAL_PREVIEW -> HORIZONTAL
            VERTICAL_PREVIEW -> VERTICAL
        }
    }

    private val fences = List(n - 1) { Array(m - 1) { FenceType.ABSENT } }

    fun playerPosition(index: Int) = players[index]
    fun currentPlayerPreviewPosition() = playersPreview[current]
    fun currentPlayerPosition() = players[current]
    fun otherPlayerPosition() = players[current xor 1]
    fun fenceAt(i: Int, j: Int) = fences.getOrNull(i)?.getOrNull(j)
    fun fenceAt(c: Cell) = fenceAt(c.y, c.x)

    fun isFinishCellFor(i: Int, j: Int, player: Int) = when (player) {
        0 -> j in 0 until m && i == n - 1
        1 -> j in 0 until m && i == 0
        else -> false
    }

    fun putFenceIfAnyLeft(i: Int, j: Int, fence: FenceType): Boolean {
        if (freeFences[current] == 0) return false
        fences[i][j] = fence
        freeFences[current]--
        finishMove()
        return true
    }

    fun previewFencePutting(i: Int, j: Int, fence: FenceType): Boolean {
        erasePreviews()
        if (freeFences[current] == 0) return false
        update()
        fences[i][j] = fence
        return true
    }

    fun movePlayerTo(cell: Cell) {
        players[current] = cell
        finishMove()
    }

    private fun erasePreviews() {
        for (i in fences.indices) {
            for (j in fences[i].indices) {
                if (fences[i][j].isPreview()) {
                    fences[i][j] = FenceType.ABSENT
                }
            }
        }
        for (i in playersPreview.indices) playersPreview[i] = null
    }

    fun previewMovePlayerTo(cell: Cell) {
        erasePreviews()
        playersPreview[current] = cell
        update()
    }

    private fun finishMove() {
        current = (current + 1) % PLAYERS
        erasePreviews()
        update()
    }

    private fun update() {
//        TODO("Not yet implemented")
    }
}
