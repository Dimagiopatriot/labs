package ui

import com.mxgraph.model.mxCell
import com.mxgraph.model.mxGraphModel
import com.mxgraph.view.mxGraph
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import javax.swing.*
import javax.swing.filechooser.FileFilter

@Suppress("UNCHECKED_CAST")
class MainWindow: JFrame("Graphs and systems") {
    private val tasksGraph = mxGraph()
    private val systemGraph = mxGraph()

    init {
        //region General setup
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(800, 600)
        isVisible = true
        //endregion

        //region Menu
        val menuBar = JMenuBar()
        jMenuBar = menuBar

        initMenu("Tasks", tasksGraph, "tas", menuBar)
        initMenu("System", systemGraph, "sys", menuBar)
        menuBar.add(JMenu("Help"))
        //endregion

        //region Tabs
        val tabbedPane = JTabbedPane()
        contentPane.add(tabbedPane)
        tabbedPane.addTab("Tasks", TasksTab(tasksGraph))
        tabbedPane.addTab("System", SystemTab(systemGraph))
        tabbedPane.addTab("Simulation", SimulationTab(tasksGraph, systemGraph))
        //endregion

    }

    private fun initMenu(target: String, graph: mxGraph, ext: String, jMenuBar: JMenuBar) {
        //region Creation of Menu
        val menu = JMenu(target)
        val menuNew = JMenuItem("New")
        val menuSave = JMenuItem("Save")
        val menuLoad = JMenuItem("Load")
        //endregion

        //region Linking Menu
        menu.add(menuNew)
        menu.add(menuSave)
        menu.add(menuLoad)
        jMenuBar.add(menu)
        //endregion

        //region Adding menu's actionListeners
        val graphModel = graph.model as mxGraphModel
        menuNew.addActionListener {
            graphModel.beginUpdate()
            graphModel.clear()
            graphModel.endUpdate()
        }
        menuSave.addActionListener {
            saveData(graphModel.cells as Map<String, mxCell>, ext)
        }
        menuLoad.addActionListener {
            loadData(ext).takeIf { data -> data.isNotEmpty() }?.let {
                graphModel.beginUpdate()
                graphModel.clear()
                graph.addCells(it)
                graphModel.endUpdate()
            }
        }
        //endregion
    }

    private fun saveData(data: Map<String, mxCell>, ext: String) {
        val chooser = JFileChooser()
        val rVal = chooser.showOpenDialog(this)

        if (rVal == JFileChooser.APPROVE_OPTION) {
            val selectedFile = chooser.selectedFile

            val file = if (selectedFile.extension == ext) selectedFile
            else File(selectedFile.path.replace(selectedFile.name, "${selectedFile.nameWithoutExtension}.$ext"))

            file.outputStream().use {
                ObjectOutputStream(it).use { it.writeObject(data.values.toTypedArray()) }
            }
        }
    }

    private fun loadData(extType: String): Array<mxCell> {
        val chooser = JFileChooser()

        class FileTypeFilter(private val extension: String, private val description: String) : FileFilter() {
            override fun accept(file: File) = if (file.isDirectory) true else file.name.endsWith(extension)
            override fun getDescription() = description + String.format(" (*%s)", extension)
        }
        chooser.addChoosableFileFilter(FileTypeFilter(".$extType", ".$extType only"))

        val rVal = chooser.showOpenDialog(this)

        return if (rVal == JFileChooser.APPROVE_OPTION) {
            lateinit var result: Array<mxCell>
            chooser.selectedFile.absoluteFile.inputStream().use {
                ObjectInputStream(it).use { result = it.readObject() as Array<mxCell> }
            }
            result
        } else emptyArray()
    }
}