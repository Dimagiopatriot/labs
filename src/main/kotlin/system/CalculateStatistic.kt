package system

import ReformattedData
import com.mxgraph.view.mxGraph
import queue.Algo12
import reformattedData
import system.schedule.Scheduler2

class CalculateStatistic(val systemGraph: mxGraph) {

    private val queueAlgos = mapOf(3 to MyAlgo3, 12 to Algo12)

    private val resultRows = mutableListOf<ResultRow>()

    fun calculate(graphModels: List<Triple<Int, Double, ReformattedData>>) {
        graphModels.forEach {
            val taskData = it.third
            val thirdAlgoTime = getAlgoTime(taskData, 3)
            val twelveAlgoTime = getAlgoTime(taskData, 12)

            val timeOnOneProcessor = taskData.matrixIdToValueMap.values.sum()

            val thirdAlgoAccelerateCoef = timeOnOneProcessor / thirdAlgoTime.toDouble()
            val twelveAlgoAccelerateCoef = timeOnOneProcessor / twelveAlgoTime.toDouble()

            val thirdAlgoSystemEfficientCoef = thirdAlgoAccelerateCoef / it.first
            val twelveAlgoSystemEfficientCoef = twelveAlgoAccelerateCoef / it.first

            val criticalPath = MyAlgo3.createQueue(taskData, true).first()

            val thirdAlgoPlanEfficiency = criticalPath / thirdAlgoTime
            val twelveAlgoPlanEfficiency = criticalPath / twelveAlgoTime

            resultRows.add(
                ResultRow(
                    it.first, it.second, "Коефіцієнт прискорення",
                    "$thirdAlgoAccelerateCoef (3)", "$twelveAlgoAccelerateCoef (12)"
                )
            )
            resultRows.add(
                ResultRow(
                    it.first, it.second, "Кoефіцієнт ефе-ті системи",
                    "$thirdAlgoSystemEfficientCoef (3)", "$twelveAlgoSystemEfficientCoef (12)"
                )
            )
            resultRows.add(
                ResultRow(
                    it.first, it.second, "Коефіцієнт ефе-ті алгоритму пл-ння",
                    "$thirdAlgoPlanEfficiency (3)", "$twelveAlgoPlanEfficiency (12)"
                )
            )
        }
    }

    fun getAlgoTime(taskData: ReformattedData, algoNumber: Int): Int {
        val system = System(systemGraph.reformattedData, 2, false)
        val queue = createQueue(algoNumber, taskData)

        system.placeTasks(taskData, queue.second.map { taskData.toMatrixId(it) }, Scheduler2)
        return system.cpus.map { it.alu.timeline.endOfTime }.max() ?: 0
    }


    private fun createQueue(algoNumber: Int, taskData: ReformattedData): Pair<Int, List<Int>> {
        val algo = queueAlgos.getValue(algoNumber)
        return algoNumber to algo.createQueue(taskData)
    }
}

data class ResultRow(
    val vertexCount: Int,
    val correlation: Double,
    val indicatorName: String,
    val thirdAlgorithmRes: String,
    val twelveAlgorithmRes: String
)