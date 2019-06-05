package system

import java.util.*
import kotlin.math.ceil

class GenerateStatistic(
    private val vertexCount: Int,
    private val vertexStep: Int,
    private val startCorrelation: Double,
    private val endCorrelation: Double,
    private val correlationStep: Double,
    private val minVertexWeight: Int,
    private val maxVertexWeight: Int,
    private var statisticVertexCount: Int
) {

    fun genStatistic() {
        var vertexCountOnIteration = vertexCount
        do {
            generateForOneGraphIteration(vertexCountOnIteration)
            vertexCountOnIteration += vertexStep
            statisticVertexCount--
        } while (statisticVertexCount > 0)
    }

    fun generateForOneGraphIteration(vertexCount: Int) {
        val vertexValues = Array(vertexCount) { getRandom(minVertexWeight, maxVertexWeight) }
        val totalVertexValue = vertexValues.sum()
        var correlationI = startCorrelation
        do {
            generateForOneCorrelationIteration(correlationI, totalVertexValue, vertexCount)
            correlationI += correlationStep
        } while (correlationI >= endCorrelation)
    }

    fun generateForOneCorrelationIteration(correlation: Double, totalVertexValue: Int, vertexCount: Int) {
        val totalEdgeValue = ceil(totalVertexValue / correlation - totalVertexValue).toInt()
        val avgEdgeValue: Int = ceil(totalEdgeValue.toDouble() / vertexCount).toInt()
    }
}

private fun getRandom(from: Int, to: Int): Int = from + Random().nextInt(to - from + 1)