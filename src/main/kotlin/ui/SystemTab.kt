package ui

import com.mxgraph.model.mxCell
import com.mxgraph.view.mxGraph
import reformattedData
import ReformattedData
import javax.swing.JButton

class SystemTab(graph: mxGraph) : CustomTab(graph, "System") {
    override val CELL_STYLE = "rounded=true;fillColor=cyan;fontSize=15;fontStyle=1"

    init {
        //Adding additional Check for lonely button
        val systemCheckButton = JButton("Check").apply {
            addActionListener { checkForLonely(graph.reformattedData) }
        }
        val generateGraphButton = JButton("Generate system").apply {
            addActionListener { GenerateGraphDialog(this@SystemTab, "Parameters", graph) }
        }
        toolsPanel.add(generateGraphButton)
        toolsPanel.add(systemCheckButton)
    }

    override fun willBeInvalidEdge(edge: mxCell): Boolean = graphCells.any {
        it.isEdge && it != edge
                && it.source.id == edge.target.id
                && it.target.id == edge.source.id
    }

    companion object {
        fun checkForLonely(sourceData: ReformattedData): Boolean {
            val (linksMatrix, matrixIdToRealIdMap) = sourceData

            linksMatrix.forEachIndexed { index, ints ->
                ints.forEachIndexed { indexInner, i ->
                    if (i != 0) linksMatrix[indexInner][index] = i
                }
            }

            fun visit(startId: Int, set: MutableSet<Int>) {
                val row = linksMatrix[startId]
                val couldVisit = row.withIndex().filter { it.value != 0 }.map { it.index }.toMutableList()

                couldVisit.removeAll(set)
                couldVisit.forEach { toVisit ->
                    set.add(toVisit)
                    visit(toVisit, set)
                }
            }

            repeat(linksMatrix.size) { startId ->
                val set = mutableSetOf<Int>()
                visit(startId, set)

                if (set.size != linksMatrix.size) {
                    println("At least #${matrixIdToRealIdMap[startId]} cant send to anyone")
                    return true
                }
            }

            return false
        }
    }
}