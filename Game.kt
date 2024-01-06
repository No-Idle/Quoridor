import State.FenceType
import State.Cell
import State.Companion.PLAYERS
import State.FenceType.*

class Game(private val n: Int, private val m: Int) {
    val state = State(n, m)

    private fun possibleMoves(cell: Cell): List<Cell> {
        fun absentFenceBetween(c1: Cell, c2: Cell): Boolean {
            val f1 = Cell(minOf(c1.x, c2.x), minOf(c1.y, c2.y))
            val f2 = Cell(maxOf(c1.x, c2.x) - 1, maxOf(c1.y, c2.y) - 1)
            val possibleObstacle = if (c1.x == c2.x) HORIZONTAL else VERTICAL
            return state.fenceAt(f1)?.withoutPreview() != possibleObstacle &&
                    state.fenceAt(f2)?.withoutPreview() != possibleObstacle
        }

        val deltas = listOf(1 to 0, 0 to 1, -1 to 0, 0 to -1)
        return deltas.map { (dx, dy) -> cell + State.Delta(dx, dy) }.filter { neighbour ->
            neighbour.y in 0 until n && neighbour.x in 0 until m && absentFenceBetween(cell, neighbour)
        }
    }

    private fun possibleMovesWithFence(cell: Cell, i: Int, j: Int, fence: FenceType): List<Cell> {
        fun absentFenceBetween(c1: Cell, c2: Cell): Boolean {
            val f1 = Cell(minOf(c1.x, c2.x), minOf(c1.y, c2.y))
            val f2 = Cell(maxOf(c1.x, c2.x) - 1, maxOf(c1.y, c2.y) - 1)
            val possibleObstacle = if (c1.x == c2.x) HORIZONTAL else VERTICAL
            if (fence == possibleObstacle && (f1 == Cell(j, i) || f2 == Cell(j, i))) return false
            return state.fenceAt(f1)?.withoutPreview() != possibleObstacle &&
                    state.fenceAt(f2)?.withoutPreview() != possibleObstacle
        }

        val deltas = listOf(0 to 1, 1 to 0, -1 to 0, 0 to -1)
        return deltas.map { (dx, dy) -> cell + State.Delta(dx, dy) }.filter { neighbour ->
            neighbour.y in 0 until n && neighbour.x in 0 until m && absentFenceBetween(cell, neighbour)
        }
    }

    private fun createGraphWithFence(i: Int, j: Int, fence: FenceType) = List(n) { y ->
        List(m) { x ->
            possibleMovesWithFence(Cell(x, y), i, j, fence)
        }
    }

    private fun reachableFrom(graph: List<List<List<Cell>>>, cell: Cell): Array<Array<Boolean>> {
        val reachable = Array(n) { Array(m) { false } }
        fun dfs(cur: Cell) {
            if (reachable[cur.y][cur.x]) return
            reachable[cur.y][cur.x] = true
            for (next in graph[cur.y][cur.x]) dfs(next)
        }
        dfs(cell)
        return reachable
    }

    private fun isValidGameWithFence(i: Int, j: Int, fence: FenceType): Boolean {
        val graph = createGraphWithFence(i, j, fence)
        val reachable = List(PLAYERS) { reachableFrom(graph, state.playerPosition(it)) }
        return (0 until PLAYERS).all { player ->
            reachable[player].indices.any { i ->
                reachable[player][i].indices.any { j -> reachable[player][i][j] && state.isFinishCellFor(i, j, player) }
            }
        }
    }

    private fun isValidMove(position: Cell?): Boolean {
        if (position == null || (0 until PLAYERS).any { state.playerPosition(it) == position }) return false
        val currentPossibleMoves = possibleMoves(state.currentPlayerPosition())
        if (position in currentPossibleMoves) return true
        if (state.otherPlayerPosition() !in currentPossibleMoves) { // jumps are not allowed
            return false
        }
        val delta = state.otherPlayerPosition() - state.currentPlayerPosition()
        val jump = state.currentPlayerPosition() + delta * 2
        val otherPossibleMoves = possibleMoves(state.otherPlayerPosition())
        if (jump in otherPossibleMoves) return position == jump
        return position in otherPossibleMoves
    }

    private fun isValidFence(i: Int, j: Int, fence: FenceType): Boolean {
        if (state.fenceAt(i, j)?.isPut() != false) return false
        val certainlyInvalid = when (fence) {
            VERTICAL, VERTICAL_PREVIEW ->
                state.fenceAt(i - 1, j) == VERTICAL || state.fenceAt(i + 1, j) == VERTICAL
            HORIZONTAL, HORIZONTAL_PREVIEW ->
                state.fenceAt(i, j - 1) == HORIZONTAL || state.fenceAt(i, j + 1) == HORIZONTAL
            ABSENT -> false
        }
        if (certainlyInvalid) return false
        return isValidGameWithFence(i, j, fence.withoutPreview())
    }

    fun moveIfValid(cell: Cell?): Boolean {
        return isValidMove(cell).also { valid -> if (valid) state.movePlayerTo(cell!!) }
    }

    fun previewMoveIfValid(cell: Cell?): Boolean {
        if (state.currentPlayerPreviewPosition() == cell) return false
        if (!isValidMove(cell)) return true
        state.previewMovePlayerTo(cell!!)
        return true
    }

    fun putFenceIfValid(i: Int, j: Int, fence: FenceType): Boolean {
        if (!fence.isPut()) return false
        if (!isValidFence(i, j, fence)) return false
        return state.putFenceIfAnyLeft(i, j, fence)
    }

    fun previewFenceIfValid(i: Int, j: Int, fence: FenceType): Boolean {
        if (state.fenceAt(i, j) == fence) return false
        if (!isValidFence(i, j, fence)) return false
        return state.previewFencePutting(i, j, fence)
    }
}
