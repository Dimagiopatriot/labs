package system

import java.util.*
import kotlin.math.ceil
import kotlin.math.max

class CPU(val matrixId: Int, val realId: Int, power: Int, externalLinks: List<ExternalLink>, internalLinksAmount: Int, private val system: System) {

    val alu = ALU(power)
    val ioUnit = IOUnit(externalLinks, internalLinksAmount)

    fun getCompletedTasksAt(tick: Int) = alu.timeline.intervals.filter { it.endTick < tick }.map(Interval<Task>::work)

    fun getExternalLinkTo(cpu: CPU) = ioUnit.externalLinks.first { it.getDestinationFor(this) == cpu }

    fun placeTask(task: Task, tick: Int) {
        val requiredTasks = task.parentTasks
        val needToReceiveTasks = requiredTasks.keys.toMutableList().apply { removeAll(getCompletedTasksAt(tick)) }

        if (needToReceiveTasks.isNotEmpty()) {
            needToReceiveTasks.forEach { taskToReceive ->
                val requestedCPU = system.getPlacedCPUForTask(taskToReceive)
                requestedCPU.initTransfer(tick, taskToReceive, task, this)
            }
        } else startPerforming(tick, task)
    }

    fun initTransfer(currentTick: Int, requestedTask: Task, suppliantTask: Task, suppliantCPU: CPU) {
        require(getCompletedTasksAt(currentTick).contains(requestedTask))
        val path = system.getPath(this, suppliantCPU)
        val transfer = Transfer(requestedTask, suppliantTask, path)
        sendTransfer(currentTick, transfer)
    }

    fun sendTransfer(currentTick: Int, transfer: Transfer) {
        ioUnit.waitingToSendQueue.add(transfer)
        ioUnit.sendOutAllPossible(currentTick)
    }

    /**
     * If transfer arrives to it's destination - startPerforming
     * */
    fun receiveTransfer(currentTick: Int, endTick: Int, transfer: Transfer, arrivalLink: IOUnit.InternalLink) {
        //must be first, or one of the first operations
        ioUnit.receive(currentTick, endTick, transfer, arrivalLink)

        val path = transfer.path
        if (path.last() != this) sendTransfer(endTick + 1, transfer)
        else {
            val requiringTask = transfer.requiringTask
            val requiredTasks = requiringTask.parentTasks.keys

            val alreadyReceivedIntervals = ioUnit.internalLinks
                .flatMap { it.receivingTimeline.intervals }
                .filter { it.work.requiringTask == requiringTask  }
            val alreadyReceivedTasks = alreadyReceivedIntervals.map { it.work.completedTask }

            val latestArrivalTick = alreadyReceivedIntervals.map(Interval<Transfer>::endTick).max()!!

            val allNecessaryTasksHere = (alreadyReceivedTasks + getCompletedTasksAt(currentTick)).containsAll(requiredTasks)
            if (allNecessaryTasksHere) startPerforming(latestArrivalTick + 1, requiringTask)
        }
    }

    fun getInternalLinkToSend(cpu: CPU, tick: Int, endTick: Int): IOUnit.InternalLink? {
        val ioUnit = this.ioUnit

        /*val usedInternalLink = ioUnit.internalLinks.firstOrNull { it.getLinkedCPUAt(tick) == cpu }
        if (usedInternalLink != null) {
            val isSending = usedInternalLink.sendingTimeline.getFirstWorkAtInterval(tick, endTick) != null
            val isReceiving = usedInternalLink.receivingTimeline.getFirstWorkAtInterval(tick, endTick) != null

            return if (isSending || (isReceiving && !system.isFullDuplex)) null else usedInternalLink
        } else return ioUnit.internalLinks.firstOrNull { it.getLinkedCPUAt(tick) == null }*/
        /*return ioUnit.internalLinks.firstOrNull { link ->
            val firstSendingInterval = link.sendingTimeline.getFirstWorkAtInterval(tick, endTick)
            val firstReceivingInterval = link.receivingTimeline.getFirstWorkAtInterval(tick, endTick)

            when {
                //sending were
                firstSendingInterval != null -> return@firstOrNull false
                //no sending were and no receiving were
                firstReceivingInterval == null -> return@firstOrNull true
                //no sending were and receiving were from particular CPU
                link.getLinkedCPUAt(firstReceivingInterval.startTick) == cpu -> return@firstOrNull system.isFullDuplex
                //no sending were and receiving were from not particular CPU
                else -> return@firstOrNull false
            }
        }*/
        val usedInternalLink = ioUnit.internalLinks.firstOrNull {
            val set = it.getLinkedCPUsAtInterval(tick, endTick)
            return@firstOrNull set.size == 1 && set.contains(cpu)
        }
        if (usedInternalLink != null) {
            val isSending = usedInternalLink.sendingTimeline.getFirstWorkAtInterval(tick, endTick) != null
            val isReceiving = usedInternalLink.receivingTimeline.getFirstWorkAtInterval(tick, endTick) != null

            return if (isSending || (isReceiving && !system.isFullDuplex)) null else usedInternalLink
        } else return ioUnit.internalLinks.firstOrNull { it.getLinkedCPUsAtInterval(tick, endTick).isEmpty() }
    }

    fun getInternalLinkToReceive(cpu: CPU, tick: Int, endTick: Int): IOUnit.InternalLink? {
        val ioUnit = this.ioUnit

        /*val usedInternalLink = ioUnit.internalLinks.firstOrNull { it.getLinkedCPUAt(tick) == cpu }
        if (usedInternalLink != null) {
            val isSending = usedInternalLink.sendingTimeline.getFirstWorkAtInterval(tick, endTick) != null
            val isReceiving = usedInternalLink.receivingTimeline.getFirstWorkAtInterval(tick, endTick) != null

            return if (isReceiving || (isSending && !system.isFullDuplex)) null else usedInternalLink
        } else return ioUnit.internalLinks.firstOrNull { it.getLinkedCPUAt(tick) == null }*/
        /*return ioUnit.internalLinks.firstOrNull { link ->
            val firstSendingInterval = link.sendingTimeline.getFirstWorkAtInterval(tick, endTick)
            val firstReceivingInterval = link.receivingTimeline.getFirstWorkAtInterval(tick, endTick)

            when {
            //receiving were
                firstReceivingInterval != null -> return@firstOrNull false
            //no receiving were and no sending were
                firstSendingInterval == null -> return@firstOrNull true
            //no receiving were and sending were from particular CPU
                link.getLinkedCPUAt(firstSendingInterval.startTick) == cpu -> return@firstOrNull system.isFullDuplex
            //no receiving were and sending were from not particular CPU
                else -> return@firstOrNull false
            }
        }*/
        val usedInternalLink = ioUnit.internalLinks.firstOrNull {
            val set = it.getLinkedCPUsAtInterval(tick, endTick)
            return@firstOrNull set.size == 1 && set.contains(cpu)
        }
        if (usedInternalLink != null) {
            val isSending = usedInternalLink.sendingTimeline.getFirstWorkAtInterval(tick, endTick) != null
            val isReceiving = usedInternalLink.receivingTimeline.getFirstWorkAtInterval(tick, endTick) != null

            return if (isReceiving || (isSending && !system.isFullDuplex)) null else usedInternalLink
        } else return ioUnit.internalLinks.firstOrNull { it.getLinkedCPUsAtInterval(tick, endTick).isEmpty() }
    }

    fun startPerforming(currentTick: Int, task: Task) {
        val startTick = max(currentTick, alu.timeline.endOfTime + 1)
        val computationTime = ceil(task.difficulty.toDouble() / alu.power).toInt()
        val computationInterval = ComputationInterval(startTick, startTick + computationTime - 1, task)
        alu.timeline.addInterval(computationInterval)
    }

    override fun toString() = "P$realId"

    //-----------------------------------------------Inner classes-----------------------------------------------

    inner class ALU(val power: Int) {
        val timeline = Timeline<Task>()
    }

    inner class IOUnit(val externalLinks: List<ExternalLink>, internalLinksAmount: Int) {
        val internalLinks = List(internalLinksAmount, { InternalLink() })
        val waitingToSendQueue = PriorityQueue<Transfer> { t1, t2 -> t2.requiringTask.priority - t1.requiringTask.priority }

        fun sendOutAllPossible(currentTick: Int) {
            val currentCPU = this@CPU

            val currentQueue = waitingToSendQueue.toMutableList()
            currentQueue.forEach { transfer: Transfer ->
                val nextCPU = transfer.getNextCPUFor(currentCPU)

                val sendingDifficulty = transfer.completedTask.childrenTasks[transfer.requiringTask]!!.toDouble()
                val externalLinkPower = getExternalLinkTo(nextCPU).power
                val sendingTime = ceil(sendingDifficulty / externalLinkPower).toInt()
                val endTick = currentTick + sendingTime - 1

                val linkToSend = getInternalLinkToSend(nextCPU, currentTick, endTick)
                val linkToReceive = nextCPU.getInternalLinkToReceive(currentCPU, currentTick, endTick)

                if (linkToSend != null && linkToReceive != null) {
                    send(currentTick, endTick, transfer, linkToSend)
                    waitingToSendQueue.remove(transfer)
                    nextCPU.receiveTransfer(currentTick, endTick, transfer, linkToReceive)
                }
            }
        }

        fun send(tick: Int, endTick: Int, transfer: Transfer, internalLink: InternalLink): TransferInterval {
            val sendingInterval = TransferInterval(tick, endTick, transfer)
            internalLink.sendingTimeline.addInterval(sendingInterval)
            return sendingInterval
        }

        fun receive(tick: Int, endTick: Int, transfer: Transfer, internalLink: InternalLink): TransferInterval {
            val receivingInterval = TransferInterval(tick, endTick, transfer)
            internalLink.receivingTimeline.addInterval(receivingInterval)
            return receivingInterval
        }

        //-----------------------------------------------InternalLink

        inner class InternalLink {
            val sendingTimeline = Timeline<Transfer>()
            val receivingTimeline = Timeline<Transfer>()

            fun getLinkedCPUAt(tick: Int): CPU? {
                val bySending = sendingTimeline.getWorkAt(tick)

                if (bySending != null) {
                    val transfer = bySending.work
                    return transfer.getNextCPUFor(this@CPU)
                } else {
                    val byReceiving = receivingTimeline.getWorkAt(tick)

                    if (byReceiving != null) {
                        val transfer = byReceiving.work
                        return transfer.getPreviousCPUFor(this@CPU)
                    }
                }

                return null
            }

            fun getLinkedCPUsAtInterval(startTick: Int, endTick: Int) = (startTick..endTick).mapNotNull { getLinkedCPUAt(it) }.toSet()
        }
    }
}