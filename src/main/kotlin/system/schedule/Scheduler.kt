package system.schedule

import system.CPU
import system.Task

typealias System = system.System

abstract class Scheduler {
    abstract fun getTaskAndCPUForPlacement(currentTick: Int, readyToPlaceTasks: List<Task>, system: System): Pair<Task, CPU>

    fun placeTasks(tasksQueue: MutableList<Task>, system: System) {
        var currentTick = 1
        var topLayerNodeCounter = 0

        val cpus = system.cpus.sortedByDescending { it.ioUnit.externalLinks.size }
        while (tasksQueue.isNotEmpty() || cpus.any { it.ioUnit.waitingToSendQueue.isNotEmpty() }) {
            val completedTasks = cpus.flatMap { it.getCompletedTasksAt(currentTick) }
            val readyToPlaceTasks = tasksQueue.filter { completedTasks.containsAll(it.parentTasks.keys) }.toMutableList()
            val freeCPUs = cpus.filter { it.alu.timeline.getWorkAt(currentTick) == null }.toMutableList()

            while (readyToPlaceTasks.isNotEmpty() && freeCPUs.isNotEmpty()) {
                var (chosenTask, chosenCPU) = getTaskAndCPUForPlacement(currentTick, readyToPlaceTasks, system)

                //tasks of top layer
                if (chosenTask.parentTasks.isEmpty()) {
                    chosenCPU = cpus[topLayerNodeCounter % cpus.size]
                    topLayerNodeCounter++
                }

                chosenCPU.placeTask(chosenTask, currentTick)
                freeCPUs.remove(chosenCPU)
                readyToPlaceTasks.remove(chosenTask)
                tasksQueue.remove(chosenTask)
            }

            cpus.forEach { it.ioUnit.sendOutAllPossible(currentTick) }

            currentTick++
        }
    }
}

object Scheduler2 : Scheduler() {
    override fun getTaskAndCPUForPlacement(currentTick: Int, readyToPlaceTasks: List<Task>, system: System): Pair<Task, CPU> {
        val task = readyToPlaceTasks.first()
        val freeCPUs = system.cpus.filter { it.alu.timeline.getWorkAt(currentTick) == null }.toMutableList()
        val mostFreeCPU = freeCPUs.minBy { it.alu.timeline.endOfTime }!!
        return task to mostFreeCPU

        /*//region Random
        val task = readyToPlaceTasks.first()
        val mostFreeCPU = system.cpus[Random().nextInt(system.cpus.size)]
        return task to mostFreeCPU
        //endregion*/
    }
}