package queue

import ReformattedData
import system.MyAlgo3
import java.util.*

interface QueueCreator {
    fun createQueue(sourceData: ReformattedData, shouldReturnCriticalPathValues: Boolean = false): List<Int>
}

object Algo12 : QueueCreator {
    override fun createQueue(sourceData: ReformattedData, shouldReturnCriticalPathValues: Boolean): List<Int> {
        /*val commonLinksMatrix = Array(sourceData.linksMatrix.size) { IntArray(sourceData.linksMatrix.size) }

        for (i in 0 until commonLinksMatrix.size) {
            for (j in 0 until commonLinksMatrix.size) {
                commonLinksMatrix[i][j] = sourceData.linksMatrix[i][j] + sourceData.reversedLinksMatrix[i][j]
            }
        }*/

        return sourceData.linksMatrix.mapIndexed { index, ints -> index to ints.sum() }
            .sortedByDescending { it.second }
            .map { sourceData.toRealId(it.first) }
    }
}