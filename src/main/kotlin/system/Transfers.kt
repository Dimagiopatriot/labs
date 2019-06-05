package system

class ExternalLink(val cpuOne: CPU, val cpuTwo: CPU, val power: Int, val isFullDuplex: Boolean) {

    fun getDestinationFor(cpu: CPU) = if (cpu == cpuOne) cpuTwo else cpuOne

    override fun toString() = "P$cpuOne<->P$cpuTwo"
}

class Transfer(val completedTask: Task, val requiringTask: Task, val path: List<CPU>) {

    fun getNextCPUFor(currentCPU: CPU): CPU {
        val currentPathPosition = path.indexOf(currentCPU)
        return path[currentPathPosition + 1]
    }

    fun getPreviousCPUFor(currentCPU: CPU): CPU {
        val currentPathPosition = path.indexOf(currentCPU)
        return path[currentPathPosition - 1]
    }

    override fun toString() = "${completedTask}→${requiringTask}•${path.first()}→${path.last()}"
}

class Task(
    val matrixId: Int,
    val realId: Int,
    val difficulty: Int,
    val priority: Int,
    val parentTasks: Map<Task, Int>,
    val childrenTasks: Map<Task, Int>
) {
    override fun toString() = "T$realId"
}