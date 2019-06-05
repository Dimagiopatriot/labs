package system

import com.mxgraph.model.mxCell
import com.mxgraph.model.mxGraphModel
import com.mxgraph.view.mxGraph
import ReformattedData
import reformattedData
import java.awt.Component
import java.util.*
import kotlin.math.ceil
import kotlin.math.round

class GenerateStatistic(
    private val vertexCount: Int,
    private val vertexStep: Int,
    private val startCorrelation: Double,
    private val endCorrelation: Double,
    private val correlationStep: Double,
    private val minVertexWeight: Int,
    private val maxVertexWeight: Int,
    private var statisticVertexCount: Int,
    private val taskGraph: mxGraph,
    private val systemGraph: mxGraph,
    private val invoker: Component
) {

    val graphModels = mutableListOf<Triple<Int, Double, ReformattedData>>()

    fun genStatistic() {
        var vertexCountOnIteration = vertexCount
        do {
            generateForOneGraphIteration(vertexCountOnIteration)
            vertexCountOnIteration += vertexStep
            statisticVertexCount--
        } while (statisticVertexCount > 0)
        val calculateStatistic = CalculateStatistic(systemGraph, invoker)
        calculateStatistic.calculate(graphModels)
    }

    fun generateForOneGraphIteration(vertexCount: Int) {
        val vertexValues = Array(vertexCount) { getRandom(minVertexWeight, maxVertexWeight) }
        val totalVertexValue = vertexValues.sum()
        var correlationI: Double = startCorrelation
        do {
            generateForOneCorrelationIteration(correlationI, totalVertexValue, vertexCount, vertexValues)
            correlationI = round((correlationI + correlationStep) * 100) / 100
        } while (correlationI <= endCorrelation)
    }

    fun generateForOneCorrelationIteration(
        correlation: Double,
        totalVertexValue: Int,
        vertexCount: Int,
        vertexValues: Array<Int>
    ) {
        val totalEdgeValue = ceil(totalVertexValue / correlation - totalVertexValue).toInt()
        val avgEdgeValue: Int = ceil(totalEdgeValue.toDouble() / vertexCount).toInt()
        val graphModel = taskGraph.model as mxGraphModel
        graphModel.beginUpdate()
        graphModel.clear()
        //endregion

        //region Creating vertexes
        var rowSize = 0
        while (rowSize * rowSize < vertexCount) rowSize++

        vertexValues.forEachIndexed { index, value ->
            val rowNumber = index / rowSize
            val inRowNumber = index % rowSize

            val distanceBetween = 100
            val newCell = taskGraph.createVertex(
                taskGraph.defaultParent, null, -1,
                50.0 + inRowNumber * distanceBetween,
                50.0 + rowNumber * distanceBetween,
                50.0, 50.0,
                ""
            ) as mxCell
            taskGraph.addCell(newCell)
            newCell.value = "${newCell.id}/$value"
        }
        //endregion

        //region Creating valid edges
        var edgeBalance = totalEdgeValue
        val connections = mutableMapOf<Pair<Int, Int>, mxCell>()

        while (edgeBalance != 0) {
            val fromId = getRandom(2, vertexCount + 1)
            val toId = getRandom(2, vertexCount + 1)
            val fromCell = graphModel.cells[fromId.toString()] as mxCell
            val toCell = graphModel.cells[toId.toString()]

            if (fromId == toId || connections.contains(fromId to toId)) continue

            val newEdge = taskGraph.insertEdge(fromCell.parent, null, -1, fromCell, toCell) as mxCell
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

        graphModels.add(Triple(vertexCount, correlation, taskGraph.reformattedData))
        graphModel.clear()
        //endregion
    }
}

private fun getRandom(from: Int, to: Int): Int = from + Random().nextInt(to - from + 1)