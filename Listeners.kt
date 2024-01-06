import java.awt.event.KeyEvent.*
import TurnTypeChooser.TurnType.*
import java.awt.Point
import java.awt.event.*

class TurnTypeChooser(private val board: Board) : KeyAdapter() {
    override fun keyPressed(e: KeyEvent) {
        turnType = when (e.keyCode) {
            VK_U, VK_UP -> MOVE_UP
            VK_L, VK_LEFT -> MOVE_LEFT
            VK_D, VK_DOWN -> MOVE_DOWN
            VK_R, VK_RIGHT -> MOVE_RIGHT
            VK_H -> PUT_HORIZONTAL
            VK_V -> PUT_VERTICAL
            else -> turnType
        }
        if (turnType in MOVE_PLAYER) {
            if (board.moveIfValid(turnType)) turnType = NOTHING
        }
    }

    enum class TurnType { NOTHING, MOVE_UP, MOVE_LEFT, MOVE_DOWN, MOVE_RIGHT, PUT_HORIZONTAL, PUT_VERTICAL }

    var turnType = NOTHING
}

val MOVE_PLAYER = listOf(MOVE_UP, MOVE_LEFT, MOVE_DOWN, MOVE_RIGHT)

class TurnCommitter(private val board: Board, private val turnTypeChooser: TurnTypeChooser) : MouseAdapter() {
    override fun mouseClicked(e: MouseEvent) {
        val turnIsSuccessfullyCommitted = when (turnTypeChooser.turnType) {
            NOTHING -> board.moveIfValid(e.x, e.y)
            PUT_HORIZONTAL -> board.putFence(e.x, e.y, State.FenceType.HORIZONTAL)
            PUT_VERTICAL -> board.putFence(e.x, e.y, State.FenceType.VERTICAL)
            else -> false
        }
        if (turnIsSuccessfullyCommitted) turnTypeChooser.turnType = NOTHING
    }
}

class MoveShower(private val board: Board, private val turnTypeChooser: TurnTypeChooser) : MouseMotionAdapter() {
    override fun mouseMoved(e: MouseEvent) {
        when (turnTypeChooser.turnType) {
            NOTHING -> board.previewMoveIfValid(e.x, e.y)
            PUT_HORIZONTAL -> board.previewPutFence(e.x, e.y, State.FenceType.HORIZONTAL_PREVIEW)
            PUT_VERTICAL -> board.previewPutFence(e.x, e.y, State.FenceType.VERTICAL_PREVIEW)
            else -> Unit
        }
    }
}
