import java.awt.Color
import java.awt.Graphics
import java.awt.GridLayout
import javax.swing.JFrame
import javax.swing.JPanel

private val boardColor = Color(223, 191, 127)
private val bordersColor = Color(255, 255, 255)
private val fenceColor = Color(127, 63, 21)
private val fencePreviewColor = Color(191, 127, 95)
private val firstPlayerColor = Color(0, 191, 0)
private val firstPlayerPreviewColor = Color(127, 223, 127)
private val secondPlayerColor = Color(127, 127, 255)
private val secondPlayerPreviewColor = Color(191, 191, 255)
private val centralPointColor = Color(0, 0, 0)

fun main() {
    Frame
}

object Frame : JFrame() {
    private const val cellSize = 140
    private const val n = 9
    private const val m = 9

    init {
        isVisible = true
        defaultCloseOperation = EXIT_ON_CLOSE

        layout = GridLayout(1, 2)
        val board = Board(n, m, cellSize)
        add(board)
        add(InfoPanel())

        val turnTypeChooser = TurnTypeChooser(board)
        addKeyListener(turnTypeChooser)
        board.addMouseListener(TurnCommitter(board, turnTypeChooser))
        board.addMouseMotionListener(MoveShower(board, turnTypeChooser))

        val (boardWidth, boardHeight) = board.bounds.run { width to height }
        val frameWidth = boardWidth + layout.minimumLayoutSize(this).width
        val frameHeight = boardHeight + layout.minimumLayoutSize(this).height
        val (screenWidth, screenHeight) = toolkit.screenSize.run { width to height }
        setBounds((screenWidth - frameWidth) / 2, (screenHeight - frameHeight) / 2, frameWidth, frameHeight)
    }
}

class InfoPanel : JPanel() {
    init {
        isVisible = true
    }
}

class Board(private val n: Int, private val m: Int, cellSize: Int) : JPanel() {
    private val bordersWidth = 2

    private val game = Game(n, m)

    init {
        setBounds(0, 0, m * cellSize + 2 * bordersWidth, n * cellSize + 2 * bordersWidth)
        isVisible = true
    }

    private fun calculateCellSize() = minOf((width - 2 * bordersWidth) / m, (height - 2 * bordersWidth) / n)

    private fun locateCell(x: Int, y: Int, cellSize: Int): State.Cell? {
        if (x - bordersWidth !in 0 until m * cellSize || y - bordersWidth !in 0 until n * cellSize) return null
        return State.Cell((x - bordersWidth) / cellSize, (y - bordersWidth) / cellSize)
    }

    private fun locateGroove(x: Int, y: Int, cellSize: Int): State.Cell? {
        if (x - bordersWidth - cellSize / 2 !in 0 until (m - 1) * cellSize ||
            y - bordersWidth - cellSize / 2 !in 0 until (n - 1) * cellSize
        ) return null
        return State.Cell((x - bordersWidth - cellSize / 2) / cellSize, (y - bordersWidth - cellSize / 2) / cellSize)
    }

    override fun paint(graphics: Graphics) {
        val cellSize = calculateCellSize()
        super.paint(graphics)
        drawGrid(cellSize, graphics)
        drawPlayers(graphics, cellSize)
        drawFences(cellSize, graphics)
    }

    private fun drawGrid(cellSize: Int, graphics: Graphics) {
        graphics.color = boardColor
        graphics.fillRect(0, 0, m * cellSize, n * cellSize)
        graphics.color = bordersColor
        for (i in 0..n) {
            graphics.fillRect(0, i * cellSize, m * cellSize + 2 * bordersWidth, 2 * bordersWidth)
        }
        for (j in 0..m) {
            graphics.fillRect(j * cellSize, 0, 2 * bordersWidth, n * cellSize + 2 * bordersWidth)
        }
    }

    private fun drawFences(cellSize: Int, graphics: Graphics) {
        for (i in 0 until n - 1) {
            for (j in 0 until m - 1) {
                if (game.state.fenceAt(i, j)?.isPut() == true) {
                    graphics.color = fenceColor
                } else if (game.state.fenceAt(i, j)?.isPreview() == true) {
                    graphics.color = fencePreviewColor
                }
                drawFence(graphics, j, i, cellSize, game.state.fenceAt(i, j))
            }
        }
    }

    private fun drawPlayers(graphics: Graphics, cellSize: Int) {
        for (player in 0 until 2) {
            val (x, y) = game.state.playerPosition(player)
            graphics.color = when (player) {
                0 -> firstPlayerColor
                else -> secondPlayerColor
            }
            drawPlayer(graphics, x, y, cellSize)
        }
        run {
            val (x, y) = game.state.currentPlayerPreviewPosition() ?: return@run
            graphics.color = when (game.state.current) {
                0 -> firstPlayerPreviewColor
                else -> secondPlayerPreviewColor
            }
            drawPlayer(graphics, x, y, cellSize)
        }
        val (x, y) = game.state.currentPlayerPosition()
        graphics.color = centralPointColor
        drawCentralPoint(graphics, x, y, cellSize)
    }

    private fun drawFence(graphics: Graphics, x: Int, y: Int, cellSize: Int, fence: State.FenceType?) {
        val shortened = cellSize / 8
        if (fence == State.FenceType.HORIZONTAL || fence == State.FenceType.HORIZONTAL_PREVIEW) {
            graphics.fillRect(
                x * cellSize + bordersWidth + shortened,
                (y + 1) * cellSize,
                2 * (cellSize - shortened),
                2 * bordersWidth
            )
        } else if (fence == State.FenceType.VERTICAL || fence == State.FenceType.VERTICAL_PREVIEW) {
            graphics.fillRect(
                (x + 1) * cellSize,
                y * cellSize + bordersWidth + shortened,
                2 * bordersWidth,
                2 * (cellSize - shortened)
            )
        }
    }

    private fun drawPlayer(graphics: Graphics, x: Int, y: Int, cellSize: Int) {
        graphics.fillOval(
            x * cellSize + bordersWidth + cellSize / 8,
            y * cellSize + bordersWidth + cellSize / 8,
            cellSize * 3 / 4,
            cellSize * 3 / 4
        )
    }

    private fun drawCentralPoint(graphics: Graphics, x: Int, y: Int, cellSize: Int) {
        graphics.fillOval(
            x * cellSize + bordersWidth + cellSize * 3 / 8,
            y * cellSize + bordersWidth + cellSize * 3 / 8,
            cellSize / 4,
            cellSize / 4
        )
    }

    fun moveIfValid(x: Int, y: Int): Boolean {
        val cellSize = calculateCellSize()
        val cell = locateCell(x, y, cellSize)
        return game.moveIfValid(cell).also { repaint() }
    }

    fun moveIfValid(turnType: TurnTypeChooser.TurnType): Boolean {
        val currentPlayer = game.state.currentPlayerPosition()
        return game.moveIfValid(
            when (turnType) {
                TurnTypeChooser.TurnType.MOVE_UP -> State.Cell(currentPlayer.x, currentPlayer.y - 1)
                TurnTypeChooser.TurnType.MOVE_LEFT -> State.Cell(currentPlayer.x - 1, currentPlayer.y)
                TurnTypeChooser.TurnType.MOVE_DOWN -> State.Cell(currentPlayer.x, currentPlayer.y + 1)
                TurnTypeChooser.TurnType.MOVE_RIGHT -> State.Cell(currentPlayer.x + 1, currentPlayer.y)
                else -> null
            }
        ).also { repaint() }
    }

    fun putFence(x: Int, y: Int, fence: State.FenceType): Boolean {
        val cellSize = calculateCellSize()
        val cell = locateGroove(x, y, cellSize) ?: return false
        val success = game.putFenceIfValid(cell.y, cell.x, fence)
        if (success) {
            repaint()
        }
        return success
    }

    fun previewPutFence(x: Int, y: Int, fenceType: State.FenceType) {
        if (!fenceType.isPreview()) return
        val cellSize = calculateCellSize()
        val cell = locateGroove(x, y, cellSize) ?: return
        if (game.previewFenceIfValid(cell.y, cell.x, fenceType)) repaint()
    }

    fun previewMoveIfValid(x: Int, y: Int) {
        val cellSize = calculateCellSize()
        val cell = locateCell(x, y, cellSize) ?: return
        if (game.previewMoveIfValid(cell)) repaint()
    }
}
