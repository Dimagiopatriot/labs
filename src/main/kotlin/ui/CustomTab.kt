package ui

import cellsCollection
import com.mxgraph.model.mxCell
import com.mxgraph.swing.mxGraphComponent
import com.mxgraph.util.mxEvent
import com.mxgraph.view.mxGraph
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

@Suppress("UNCHECKED_CAST")
abstract class CustomTab(private val graph: mxGraph, private val type: String = "Tasks") : JPanel() {

    protected val graphCells
        get() = graph.cellsCollection

    protected val toolsPanel = JPanel().apply { maximumSize = Dimension(700, 36) }

    abstract val CELL_STYLE: String
    private val DEFAULT_CELL_VALUE = 1

    abstract fun willBeInvalidEdge(edge: mxCell): Boolean

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        val graphComponent = mxGraphComponent(graph)
        add(graphComponent)

        //region Adding base tools panel
        toolsPanel.add(JLabel("Value: "))

        val valueField = JTextField("Value", 4).apply { isEnabled = false }
        toolsPanel.add(valueField)

        val addButton = JButton("Add vertex").apply {
            addActionListener {
                val newCell = graph.createVertex(
                    graph.defaultParent, null, -1,
                    50.0, 50.0, 50.0, 50.0, CELL_STYLE
                ) as mxCell
                graph.model.beginUpdate()
                graph.addCell(newCell)
                newCell.value = "${newCell.id}/${1}"
                graph.model.endUpdate()
            }
        }

        toolsPanel.add(addButton)

        val updateButton = JButton("Update").apply { isEnabled = false }
        updateButton.addActionListener { _ ->
            val cell = graph.selectionCell as mxCell
            val cellNewValue = if (cell.isEdge) valueField.text.toInt()
            else (cell.value as String).split('/')[0] + "/" + valueField.text
            cell.value = cellNewValue
            graph.refresh()
        }
        toolsPanel.add(updateButton)

        add(toolsPanel)
        //endregion

        with(graph) {
            isKeepEdgesInBackground = true
            isCellsResizable = false
            isCellsEditable = false
            isEdgeLabelsMovable = false
            isAllowDanglingEdges = false
        }
        val graphParent = graph.defaultParent

        //region Mouse events
        graphComponent.graphControl.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val cell = graphComponent.getCellAt(e.x, e.y) as mxCell?

                when {
                    //add new cell
                    e.x < 30 && e.y < 30 -> {
                        val newCell = graph.createVertex(
                            graphParent, null, -1,
                            50.0, 50.0, 50.0, 50.0, CELL_STYLE
                        ) as mxCell
                        graph.model.beginUpdate()
                        graph.addCell(newCell)
                        newCell.value = "${newCell.id}/$DEFAULT_CELL_VALUE"
                        graph.model.endUpdate()
                    }
                    //update field's value
                    cell != null -> {
                        valueField.isEnabled = true
                        updateButton.isEnabled = true
                        val newFieldText =
                            if (cell.isEdge) cell.value.toString() else (cell.value as String).split('/')[1]
                        valueField.text = newFieldText
                    }
                    //disabling field
                    else -> {
                        valueField.isEnabled = false
                        updateButton.isEnabled = false
                    }
                }
            }
        })

        //Button delete cell
        graphComponent.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                val cell = graph.selectionCell as mxCell?
                if (e.keyCode == 127 && cell != null) {
                    graph.getEdges(cell).forEach { graph.model.remove(it) }
                    graph.model.remove(cell)
                }
            }
        })
        //endregion

        //region Connection complex listener
        graph.addListener(mxEvent.CELL_CONNECTED) { a, eventObject ->
            val graph = a as mxGraph
            val cell = eventObject.properties["edge"] as mxCell
            cell.value = DEFAULT_CELL_VALUE
            if (type == "System") {
                cell.style = "endArrow=none"
            }

            if (eventObject.properties["source"] == false) {
                val sourceId = cell.source.id
                val targetId = cell.target.id

                val alreadyExists = graphCells.any {
                    it.isEdge && it != cell
                            && it.source.id == sourceId
                            && it.target.id == targetId
                }

                if (alreadyExists || willBeInvalidEdge(cell)) graph.model.remove(cell)
            }
        }
        //endregion
    }
}