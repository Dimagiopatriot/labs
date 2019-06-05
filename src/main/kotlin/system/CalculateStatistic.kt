package system

import ReformattedData
import com.mxgraph.view.mxGraph
import queue.Algo12
import reformattedData
import system.schedule.Scheduler2
import ui.ShowStatisticDialog
import java.awt.Component
import kotlin.math.round

class CalculateStatistic(val systemGraph: mxGraph, val invoker: Component) {

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

            val thirdAlgoSystemEfficientCoef = thirdAlgoAccelerateCoef / it.first.toDouble()
            val twelveAlgoSystemEfficientCoef = twelveAlgoAccelerateCoef / it.first.toDouble()

            val criticalPath = MyAlgo3.createQueue(taskData, true).first()

            val thirdAlgoPlanEfficiency = criticalPath / thirdAlgoTime.toDouble()
            val twelveAlgoPlanEfficiency = criticalPath / twelveAlgoTime.toDouble()

            resultRows.add(
                ResultRow(
                    it.first, it.second, "К. пр. ",
                    "${myRound(thirdAlgoAccelerateCoef)} (3)", "${myRound(twelveAlgoAccelerateCoef)} (12)"
                )
            )
            resultRows.add(
                ResultRow(
                    it.first, it.second, "К. еф. ",
                    "${myRound(thirdAlgoSystemEfficientCoef)} (3)", "${myRound(twelveAlgoSystemEfficientCoef)} (12)"
                )
            )
            resultRows.add(
                ResultRow(
                    it.first, it.second, "К. еф. пл.",
                    "${myRound(thirdAlgoPlanEfficiency)} (3)", "${myRound(twelveAlgoPlanEfficiency)} (12)"
                )
            )
        }
        ShowStatisticDialog(invoker, "Statistic", resultRows)
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

fun myRound(d: Double): Double {
    val newDouble = d * 1000
    val i = round(newDouble)
    return i / 1000.toDouble()
}