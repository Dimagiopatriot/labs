package ui

import com.mxgraph.view.mxGraph
import system.GenerateStatistic
import java.awt.Component
import java.awt.Point
import javax.swing.*

class GenerateStatisticDialog(private val invoker: Component, title: String, taskGraph: mxGraph, systemGraph: mxGraph) :
    JDialog(SwingUtilities.windowForComponent(invoker), title) {

    private val vertexStartCount = JTextField("8", 4)
    private val vertexStep = JTextField("8", 4)
    private val startCorrelationField = JTextField("0.5", 4)
    private val endCorrelationField = JTextField("0.9", 4)
    private val correlationStep = JTextField("0.1", 4)
    private val minVertexWeight = JTextField("1", 4)
    private val maxVertexWeight = JTextField("10", 4)
    private val statisticVertexCount = JTextField("2", 4)


    init {
        layout = BoxLayout(contentPane, BoxLayout.Y_AXIS)
        defaultCloseOperation = DISPOSE_ON_CLOSE
        isResizable = false

        val ownerSize = owner.size
        val point = owner.location
        location = Point(point.x + ownerSize.width / 2, point.y + ownerSize.height / 2)

        val minLine = JPanel()
        val minLabel = JLabel("Vertex start value")
        minLine.add(minLabel)
        minLine.add(vertexStartCount)
        add(minLine)

        val amountLine = JPanel()
        val amountLabel = JLabel("Vertex step")
        amountLine.add(amountLabel)
        amountLine.add(vertexStep)
        add(amountLine)

        val correlationLine = JPanel()
        val correlationLabel = JLabel("Start correlation value")
        correlationLine.add(correlationLabel)
        correlationLine.add(startCorrelationField)
        add(correlationLine)

        val endCorrelationLine = JPanel()
        val endCorrelationLabel = JLabel("End correlation value")
        endCorrelationLine.add(endCorrelationLabel)
        endCorrelationLine.add(endCorrelationField)
        add(endCorrelationLine)

        val correlationStepLine = JPanel()
        val correlationStepLabel = JLabel("Correlation step value")
        correlationStepLine.add(correlationStepLabel)
        correlationStepLine.add(correlationStep)
        add(correlationStepLine)

        val minVertexLine = JPanel()
        val minVertexLabel = JLabel("Min vertex value")
        minVertexLine.add(minVertexLabel)
        minVertexLine.add(minVertexWeight)
        add(minVertexLine)

        val maxVertexLine = JPanel()
        val maxVertexLabel = JLabel("Max vertex value")
        maxVertexLine.add(maxVertexLabel)
        maxVertexLine.add(maxVertexWeight)
        add(maxVertexLine)

        val staticVertexCountLine = JPanel()
        val statisticVertexLabel = JLabel("Statistic vertex value")
        staticVertexCountLine.add(statisticVertexLabel)
        staticVertexCountLine.add(statisticVertexCount)
        add(staticVertexCountLine)

        val buttonsLine = JPanel()
        val generateButton = JButton("Generate").apply {
            addActionListener {
                val genStatistic = GenerateStatistic(
                    vertexStartCount.text.toInt(),
                    vertexStep.text.toInt(),
                    startCorrelationField.text.toDouble(),
                    endCorrelationField.text.toDouble(),
                    correlationStep.text.toDouble(),
                    minVertexWeight.text.toInt(),
                    maxVertexWeight.text.toInt(),
                    statisticVertexCount.text.toInt(),
                    taskGraph,
                    systemGraph
                )
                genStatistic.genStatistic()
            }
        }
        val cancelButton = JButton("Cancel").apply { addActionListener { isVisible = false; dispose() } }
        buttonsLine.add(generateButton)
        buttonsLine.add(cancelButton)
        add(buttonsLine)

        pack()
        location = Point(location.x - size.width / 2, location.y - size.height / 2)
        isVisible = true
    }
}