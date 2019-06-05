package ui

import system.ResultRow
import java.awt.Component
import java.awt.Point
import javax.swing.*

class ShowStatisticDialog(private val invoker: Component, title: String, rowsToOutput: List<ResultRow>) :
    JDialog(SwingUtilities.windowForComponent(invoker), title) {

    init {
        layout = BoxLayout(contentPane, BoxLayout.Y_AXIS)
        defaultCloseOperation = DISPOSE_ON_CLOSE
        isResizable = false

        val ownerSize = owner.size
        val point = owner.location
        location = Point(point.x + ownerSize.width / 2, point.y + ownerSize.height / 2)

        val panel = JPanel()
        panel.setSize(800, 600)
        val scrollPane = JPanel()
        val columnNames = arrayOf("Кореляція", "Назва показнику", "3 алгоритм", "12 алгоритм")

        val gropedRows = rowsToOutput.groupBy { it.vertexCount }
        val sortedRows = gropedRows.mapValues { it.value.sortedBy { it.correlation } }
        sortedRows
            .mapValues {
                it.value.map { v ->
                    arrayOf(v.correlation.toString(), v.indicatorName, v.thirdAlgorithmRes, v.twelveAlgorithmRes)
                }
            }
            .forEach {
                val vertexCountLabel = JLabel("Для графу з ${it.key} вершинами")
                scrollPane.add(vertexCountLabel)

                val table = JTable(it.value.toTypedArray(), columnNames)
                table.setBounds(30, 40, 200, 300)
                scrollPane.add(table)
            }
//        panel.add(scrollPane)
        scrollPane.isVisible = true

        add(scrollPane)
        setSize(300, 300)
        pack()
        location = Point(location.x - size.width / 2, location.y - size.height / 2)
        isVisible = true
    }
}