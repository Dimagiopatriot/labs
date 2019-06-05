package system

import ReformattedData
import queue.QueueCreator

object MyAlgo3 : QueueCreator {
    //алгоритм такий - ми беремо найнижчі вершини, проходячи вверх, записуємо в список наступні зі значенями їх ваг + ваги попередньої
    // якщо записана вага існує вже в списку, але менша, ніж вага даної вершини в списку на даний момент, то перезаписуємо. Інакше,
    // лишаємо без змін
    private val criticalPaths = mutableSetOf<Pair<Int, Int>>()

    override fun createQueue(sourceData: ReformattedData, shouldReturnCriticalPathValues: Boolean): List<Int> {
        criticalPaths.clear()

        val endVertexes =
            sourceData.linksMatrix.mapIndexed { index, ints -> index to ints }
                .filter { it.second.sum() == 0 }
                .map { sourceData.toRealId(it.first) }

        endVertexes.forEach {
            criticalPaths.add(Pair(it, sourceData.matrixIdToValueMap.getValue(sourceData.toMatrixId(it))))
        }

        var firstParent = findCriticalPaths(endVertexes, sourceData)

        while (firstParent.isNotEmpty()) {
            firstParent = findCriticalPaths(firstParent.toList(), sourceData)
        }

        return if (!shouldReturnCriticalPathValues)
            criticalPaths.sortedByDescending { it.second }.map { it.first }
        else
            criticalPaths.sortedByDescending { it.second }.map { it.second }
    }

    private fun findCriticalPaths(vertexes: List<Int>, sourceData: ReformattedData): Set<Int> {
        val newVertexes = mutableSetOf<Int>()
        vertexes.forEach { childVertex ->
            val matrixIdsOfParentVertexes = sourceData.reversedLinksMatrix[sourceData.toMatrixId(childVertex)]
                .mapIndexed { index, it -> Pair(index, it) }
                .filter { it.second != 0 }

            val newCriticalPaths = matrixIdsOfParentVertexes
                .map {
                    val weight = criticalPaths.first { it.first == childVertex }.second +
                            sourceData.matrixIdToValueMap.getValue(it.first)
                    Pair(sourceData.toRealId(it.first), weight)
                }
                .filter { toFilter -> toFilter.second > criticalPaths.find { it.first == toFilter.first }?.second ?: -200 }

            newCriticalPaths.forEach { newCriticalPath ->
                val finded = criticalPaths.find { it.first == newCriticalPath.first }
                if (finded == null) {
                    criticalPaths.add(newCriticalPath)
                } else if (finded.second < newCriticalPath.second) {
                    criticalPaths.remove(finded)
                    criticalPaths.add(newCriticalPath)
                }
            }

            newVertexes.addAll(matrixIdsOfParentVertexes.map { sourceData.toRealId(it.first) })
        }
        return newVertexes
    }
}