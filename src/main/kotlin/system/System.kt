package system

import java.util.*
import ReformattedData
import system.schedule.Scheduler

class System(inputData: ReformattedData, val internalLinksAmount: Int, val isFullDuplex: Boolean) {

    val cpus: List<CPU>

    init {
        val (linksMatrix, _, matrixIdToValueMap) = inputData

        val processorToItExternalLinksMap = mutableMapOf<CPU, MutableList<ExternalLink>>()
        cpus = List(linksMatrix.size, { cpuId ->
            val externalLinks = mutableListOf<ExternalLink>()

            val cpu = CPU(cpuId, inputData.toRealId(cpuId), matrixIdToValueMap[cpuId]!!, externalLinks, internalLinksAmount, this)
            processorToItExternalLinksMap[cpu] = externalLinks
            return@List cpu
        })

        val externalLinks = mutableListOf<ExternalLink>()
        linksMatrix.forEachIndexed { rowIndex, ints ->
            ints.forEachIndexed { index, power ->
                if (power != 0) externalLinks.add(ExternalLink(
                    cpus.first { it.matrixId == rowIndex },
                    cpus.first { it.matrixId == index },
                    power,
                    isFullDuplex
                ))
            }
        }

        processorToItExternalLinksMap.forEach { cpu, links ->
            links.addAll(externalLinks.filter { it.cpuOne == cpu || it.cpuTwo == cpu })
        }
    }

    fun getPlacedCPUForTask(task: Task) = cpus.first { it.alu.timeline.intervals.map(Interval<Task>::work).contains(task) }

    fun getPath(fromCPU: CPU, toCPU: CPU): List<CPU> {
        var bestPathCost = 0
        var path = listOf(fromCPU)

        fun visit(startCPU: CPU, stack: Stack<CPU>, initialPathCost: Int) {
            val links = startCPU.ioUnit.externalLinks

            links.forEach { link ->
                val destination = link.getDestinationFor(startCPU)
                if (stack.contains(destination))
                    return@forEach

                val currentPathCost = initialPathCost + link.power
                stack.push(destination)

                if (destination == toCPU) {
                    if (currentPathCost > bestPathCost || (currentPathCost == bestPathCost && stack.size < path.size)) {
                        bestPathCost = currentPathCost
                        path = stack.toList()
                        stack.pop()
                        return@forEach
                    }
                } else visit(destination, stack, currentPathCost)

                stack.pop()
            }
        }

        val stack = Stack<CPU>().apply { push(fromCPU) }
        visit(fromCPU, stack, 0)

        return path
    }

    fun placeTasks(inputData: ReformattedData, queue: List<Int>, scheduler: Scheduler) {
        val (linksMatrix, _, matrixIdToValueMap) = inputData
        val taskAndItsConnectionsMap = mutableMapOf<Int, Triple<Task, MutableMap<Task, Int>, MutableMap<Task, Int>>>()

        //region Creating Tasks queue
        val tasksQueue = queue.mapIndexed { index, taskId ->
            val parentTasks = mutableMapOf<Task, Int>()
            val childrenTasks = mutableMapOf<Task, Int>()
            val task = Task(taskId, inputData.toRealId(taskId), matrixIdToValueMap[taskId]!!, queue.size - index, parentTasks, childrenTasks)
            taskAndItsConnectionsMap[taskId] = Triple(task, parentTasks, childrenTasks)
            return@mapIndexed task
        }.toMutableList()

        taskAndItsConnectionsMap.forEach { fromTaskId, triple ->
            val (fromTask, _, fromChildrenTasks) = triple
            val row = linksMatrix[fromTaskId]
            row.forEachIndexed { toTaskId, weight ->
                if (weight != 0) {
                    val (toTask, toParentTasks) = taskAndItsConnectionsMap[toTaskId]!!
                    fromChildrenTasks[toTask] = weight
                    toParentTasks[fromTask] = weight
                }
            }
        }
        //endregion

        scheduler.placeTasks(tasksQueue, this)
    }

}