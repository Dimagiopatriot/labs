package ui

import com.mxgraph.model.mxCell
import com.mxgraph.model.mxGraphModel
import com.mxgraph.view.mxGraph
import java.awt.Point
import java.util.*
import javax.swing.*
import kotlin.math.ceil

class GenerateGraphDialog(private val invoker: CustomTab, title: String, graph: mxGraph) : JDialog(SwingUtilities.windowForComponent(invoker), title) {

    private val minField = JTextField("1", 4)
    private val maxField = JTextField("8", 4)
    private val amountField = JTextField("10", 4)
    private val correlationField = JTextField("0.5", 4)

    init {
        layout = BoxLayout(contentPane, BoxLayout.Y_AXIS)
        defaultCloseOperation = DISPOSE_ON_CLOSE
        isResizable = false

        val ownerSize = owner.size
        val point = owner.location
        location = Point(point.x + ownerSize.width / 2, point.y + ownerSize.height / 2)

        val minLine = JPanel()
        val minLabel = JLabel("Vertex min value")
        minLine.add(minLabel)
        minLine.add(minField)
        add(minLine)

        val maxLine = JPanel()
        val maxLabel = JLabel("Vertex max value")
        maxLine.add(maxLabel)
        maxLine.add(maxField)
        add(maxLine)

        val amountLine = JPanel()
        val amountLabel = JLabel("Amount of vertexes")
        amountLine.add(amountLabel)
        amountLine.add(amountField)
        add(amountLine)

        val correlationLine = JPanel()
        val correlationLabel = JLabel("Correlation value")
        correlationLine.add(correlationLabel)
        correlationLine.add(correlationField)
        add(correlationLine)

        val buttonsLine = JPanel()
        val generateButton = JButton("Generate").apply { addActionListener { generateGraph(graph) } }
        val cancelButton = JButton("Cancel").apply { addActionListener { isVisible = false; dispose() } }
        buttonsLine.add(generateButton)
        buttonsLine.add(cancelButton)
        add(buttonsLine)

        pack()
        location = Point(location.x - size.width / 2, location.y - size.height / 2)
        isVisible = true
    }

    private fun generateGraph(graph: mxGraph) {
        //region Preparation stage
        val minVertexValue = minField.text.toInt()
        val maxVertexValue = maxField.text.toInt()
        val amountOfVertexes = amountField.text.toInt()
        val correlation = correlationField.text.toDouble()

        val vertexValues = Array(amountOfVertexes) { getRandom(minVertexValue, maxVertexValue) }
        val totalVertexValue = vertexValues.sum()
        val totalEdgeValue = ceil(totalVertexValue / correlation - totalVertexValue).toInt()
        val avgEdgeValue: Int = ceil(totalEdgeValue.toDouble() / amountOfVertexes).toInt()

        val graphModel = graph.model as mxGraphModel
        graphModel.beginUpdate()
        graphModel.clear()
        //endregion

        //region Creating vertexes
        var rowSize = 0
        while (rowSize * rowSize < amountOfVertexes) rowSize++

        vertexValues.forEachIndexed { index, value ->
            val rowNumber = index / rowSize
            val inRowNumber = index % rowSize

            val distanceBetween = 100
            val newCell = graph.createVertex(graph.defaultParent, null, -1,
                50.0 + inRowNumber * distanceBetween,
                50.0 + rowNumber * distanceBetween,
                50.0, 50.0,
                invoker.CELL_STYLE) as mxCell
            graph.addCell(newCell)
            newCell.value = "${newCell.id}/$value"
        }
        //endregion

        //region Creating valid edges
        var edgeBalance = totalEdgeValue
        val connections = mutableMapOf<Pair<Int, Int>, mxCell>()

        while (edgeBalance != 0) {
            val fromId = getRandom(2, amountOfVertexes + 1)
            val toId = getRandom(2, amountOfVertexes + 1)
            val fromCell = graphModel.cells[fromId.toString()] as mxCell
            val toCell = graphModel.cells[toId.toString()]

            if (fromId == toId || connections.contains(fromId to toId)) continue

            val newEdge = graph.insertEdge(fromCell.parent, null, -1, fromCell, toCell) as mxCell
            val isAcceptable = graphModel.cells.containsValue(newEdge)

            if (isAcceptable) {
                var edgeValue = getRandom(1, avgEdgeValue)
                if (edgeBalance - edgeValue < 0) edgeValue = edgeBalance
                newEdge.value = edgeValue

                connections[fromId to toId] = newEdge
                edgeBalance -= edgeValue
            }
        }
        graphModel.endUpdate()
        //endregion

        connections.values.sumBy { it.value as Int }
            .apply { println("Total vertexes: $totalVertexValue; edge: $totalEdgeValue; real: $this") }
        isVisible = false
        dispose()
    }

    private fun getRandom(from: Int, to: Int): Int = from + Random().nextInt(to - from + 1)
}