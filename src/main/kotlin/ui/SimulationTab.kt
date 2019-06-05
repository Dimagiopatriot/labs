package ui

import com.mxgraph.view.mxGraph
import queue.Algo12
import reformattedData
import system.MyAlgo3
import system.gantt.GanttDiagram
import system.schedule.Scheduler2
import java.awt.Dimension
import javax.swing.*

typealias System = system.System

class SimulationTab(private val tasksGraph: mxGraph, private val systemGraph: mxGraph) : JPanel() {

    private val taskData
        get() = tasksGraph.reformattedData

    private val systemData
        get() = systemGraph.reformattedData

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        val gnattDiagram = GanttDiagram()
        add(JScrollPane(gnattDiagram))

        val toolsPanel = JPanel().apply { maximumSize = Dimension(700, 36) }
        //region Adding queue tools to panel

        val createQueueButton = JButton("Create queue")
        toolsPanel.add(createQueueButton)

        val thirdAlgoRB = JRadioButton("3").apply { actionCommand = text }
        val twelveAlgoRB = JRadioButton("12").apply { actionCommand = text }
        val queueSelector = ButtonGroup().apply {
            add(thirdAlgoRB)
            add(twelveAlgoRB)
        }
        toolsPanel.apply {
            add(thirdAlgoRB)
            add(twelveAlgoRB)
        }
        createQueueButton.addActionListener {
            val pair = createQueue(queueSelector)
            pair.second
                .joinToString(
                    prefix = "Queue by ${queueSelector.selection.actionCommand} algorithm: ",
                    separator = " > "
                )
                .apply(::println)
        }

        add(toolsPanel)
        //endregion

        //region Adding modeling tools to panel
        toolsPanel.add(JLabel("    Links: "))

        val linksField = JTextField("2", 4)
        toolsPanel.add(linksField)

        val isFullDuplexCheckbox = JCheckBox("Full duplex")
        toolsPanel.add(isFullDuplexCheckbox)

        val secondSchedulerRB = JRadioButton("2", true).apply { actionCommand = text }
        val schedulerSelector = ButtonGroup().apply {
            add(secondSchedulerRB)
        }
        toolsPanel.apply {
            add(secondSchedulerRB)
        }

        val simulateButton = JButton("Simulate")
        simulateButton.addActionListener {
            if (SystemTab.checkForLonely(systemData)) return@addActionListener

            val schedulerNumber = schedulerSelector.selection.actionCommand
            val scheduler = schedulerAlgos.getValue(schedulerNumber)
            val queue = createQueue(queueSelector)
            val system = System(systemData, linksField.text.toInt(), isFullDuplexCheckbox.isSelected)

            system.placeTasks(taskData, queue.second.map { taskData.toMatrixId(it) }, scheduler)
            gnattDiagram.repaintForSystem(system)
        }
        toolsPanel.add(simulateButton)

        add(toolsPanel)
        //endregion
    }

    private val schedulerAlgos = mapOf("2" to Scheduler2)

    private val queueAlgos = mapOf("3" to MyAlgo3, "12" to Algo12)

    private fun createQueue(queueSelector: ButtonGroup): Pair<Int, List<Int>> {
        val algoNumber = queueSelector.selection.actionCommand
        val algo = queueAlgos.getValue(algoNumber)
        return algoNumber.toInt() to algo.createQueue(/*if (algo is MyAlgo3)*/ taskData /*else systemData*/)
    }
}