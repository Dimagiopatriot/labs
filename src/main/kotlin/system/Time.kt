package system

class Timeline<T>(private val intervalsMutable: MutableList<Interval<T>> = mutableListOf()) {

    val intervals: List<Interval<T>> get() = intervalsMutable

    val endOfTime
        get() = intervalsMutable.maxBy { it.endTick }?.endTick ?: 0

    fun addInterval(interval: Interval<T>) {
        val allOk = intervalsMutable.none {
            interval.startTick >= it.startTick && interval.startTick <= it.endTick
                    || interval.endTick >= it.startTick && interval.endTick <= it.endTick
        }
        require(allOk, { "Interception of intervals" })

        intervalsMutable.add(interval)
    }

    fun getWorkAt(time: Int): Interval<T>? = intervalsMutable.firstOrNull { it.startTick <= time && it.endTick >= time }

    fun getFirstWorkAtInterval(startTick: Int, endTick: Int): Interval<T>? = intervalsMutable.firstOrNull { interval ->
        val isNoIntersections = interval.startTick > endTick || interval.endTick < startTick
        return@firstOrNull !isNoIntersections
    }
}

abstract class Interval<T>(val startTick: Int, val endTick: Int, val work: T) {
    val length
        get() = endTick - startTick + 1

    init {
        if (startTick > endTick) throw IllegalArgumentException("Start tick:$startTick bigger than end tick:$endTick")
    }

    override fun toString() = "${startTick}t to ${endTick}t work:[$work]"
}

class ComputationInterval(startTick: Int, endTick: Int, task: Task) : Interval<Task>(startTick, endTick, task)

class TransferInterval(startTick: Int, endTick: Int, transfer: Transfer) : Interval<Transfer>(startTick, endTick, transfer)