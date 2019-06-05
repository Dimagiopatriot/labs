package ui

import com.mxgraph.model.mxCell
import com.mxgraph.view.mxGraph
import reformatData
import java.util.*
import javax.swing.JButton

class TasksTab(graph: mxGraph): CustomTab(graph) {
    override val CELL_STYLE = "shape=ellipse;fillColor=lightgreen;fontSize=15;fontStyle=1"

    init {
        val generateGraphButton = JButton("Generate graph").apply {
            addActionListener { GenerateGraphDialog(this@TasksTab, "Parameters", graph) }
        }
        toolsPanel.add(generateGraphButton)
    }

    override fun willBeInvalidEdge(edge: mxCell) = checkForLoop(graphCells)

    private fun checkForLoop(graphCells: Collection<mxCell>): Boolean {
        val (linksMatrix, matrixIdToRealIdMap) = reformatData(graphCells)

        fun visit(startId: Int, stack: Stack<Int>): Boolean {
            val row = linksMatrix[startId]
            val couldVisit = row.withIndex().filter { it.value != 0 }.map { it.index }

            if (stack.intersect(couldVisit).isNotEmpty()) {
                println("Loop: ${stack.map { matrixIdToRealIdMap[it] }.joinToString(separator = " > ")}")
                return true
            } else {
                couldVisit.forEach { toVisit ->
                    stack.push(toVisit)
                    val willBeLoop = visit(toVisit, stack)
                    if (willBeLoop) return true else stack.pop()
                }
            }

            return false
        }

        val stack = Stack<Int>()
        repeat(linksMatrix.size) { startId -> if (visit(startId, stack)) return true }

        return false
    }
}