package system.gantt

import reformatData
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Rectangle
import javax.swing.JComponent

typealias System = system.System

@Suppress("PrivatePropertyName")
class GanttDiagram(private var system: System) : JComponent() {

    constructor() : this(System(reformatData(emptyList()), 0, false))

    private val TIC_HEIGHT = 20
    private val ALU_WIDTH = 35
    private val IO_WIDTH = 90
    private val BORDERS_PADDING = 40
    private val TEXT_HEIGHT = 20

    private val HEADER_HEIGHT = TEXT_HEIGHT * 2
    private val HEADER_OFFSET = BORDERS_PADDING + HEADER_HEIGHT
    private val processorWidth get() = ALU_WIDTH + 2 * IO_WIDTH * system.internalLinksAmount

    private val processorsNumber get() = system.cpus.size
    private val ticsCount get() = system.cpus.map { it.alu.timeline.endOfTime }.max() ?: 0

    override fun getWidth() = 2 * BORDERS_PADDING + processorsNumber * processorWidth
    override fun getHeight() = 2 * BORDERS_PADDING + ticsCount * TIC_HEIGHT + TEXT_HEIGHT * 2 + HEADER_HEIGHT
    override fun getPreferredSize() = Dimension(width, height)

    fun repaintForSystem(system: System) {
        this.system = system
        repaint()
        revalidate()
    }

    override fun paint(g: Graphics) {
        if (ticsCount != 0) {
            drawSkeleton(g)
            drawBorderNumbers(g)
            drawWork(g)
            drawWorkTotal(g)
            g.dispose()
        }
    }

    private fun drawSkeleton(g: Graphics) {
        g.drawLine(BORDERS_PADDING, BORDERS_PADDING, BORDERS_PADDING + processorsNumber * processorWidth, BORDERS_PADDING)

        for (i in system.cpus.indices) {
            val leftLineXPos = BORDERS_PADDING + i * processorWidth
            val rightLineXPos = leftLineXPos + processorWidth
            g.drawLine(leftLineXPos, BORDERS_PADDING, leftLineXPos, BORDERS_PADDING + ticsCount * TIC_HEIGHT + HEADER_HEIGHT)
            g.drawLine(rightLineXPos, BORDERS_PADDING, rightLineXPos, BORDERS_PADDING + ticsCount * TIC_HEIGHT + HEADER_HEIGHT)
        }
    }

    private fun drawBorderNumbers(g: Graphics) {
        for ((cpuIndex, cpu) in system.cpus.withIndex()) {

            val processorRect = Rectangle(BORDERS_PADDING + cpuIndex * processorWidth, BORDERS_PADDING - (TEXT_HEIGHT * 1.5).toInt(), processorWidth, TEXT_HEIGHT)
            val processorText = "Processor #${cpu.realId}"
            drawStringCentred(g, processorRect, processorText)

            val aluRect = Rectangle(getALUOffset(cpuIndex), BORDERS_PADDING, ALU_WIDTH, 2 * TEXT_HEIGHT)
            val aluText = "Task"
            g.drawRect(getALUOffset(cpuIndex), BORDERS_PADDING, ALU_WIDTH, 2 * TEXT_HEIGHT)
            drawStringCentred(g, aluRect, aluText)

            val aluPadding = getALUOffset(cpuIndex) + ALU_WIDTH
            repeat(system.internalLinksAmount) { linkIndex ->

                val linkRect = Rectangle(aluPadding + linkIndex * 2 * IO_WIDTH, BORDERS_PADDING, 2 * IO_WIDTH, TEXT_HEIGHT)
                val linkText = "Link ${linkIndex + 1}"
                g.drawRect(aluPadding + linkIndex * 2 * IO_WIDTH, BORDERS_PADDING, 2 * IO_WIDTH, TEXT_HEIGHT)
                drawStringCentred(g, linkRect, linkText)

                val sendRect = Rectangle(getSendingOffset(cpuIndex, linkIndex), BORDERS_PADDING + TEXT_HEIGHT, IO_WIDTH, TEXT_HEIGHT)
                val sendText = "Sending"
                g.drawRect(getSendingOffset(cpuIndex, linkIndex), BORDERS_PADDING + TEXT_HEIGHT, IO_WIDTH, TEXT_HEIGHT)
                drawStringCentred(g, sendRect, sendText)

                val receiveRect = Rectangle(getReceivingOffset(cpuIndex, linkIndex), BORDERS_PADDING + TEXT_HEIGHT, IO_WIDTH, TEXT_HEIGHT)
                val receiveText = "Receiving"
                g.drawRect(getReceivingOffset(cpuIndex, linkIndex), BORDERS_PADDING + TEXT_HEIGHT, IO_WIDTH, TEXT_HEIGHT)
                drawStringCentred(g, receiveRect, receiveText)
            }
        }

        repeat(ticsCount) {
            val tick = it + 1
            val rect = Rectangle(BORDERS_PADDING - ALU_WIDTH, HEADER_OFFSET + it * TIC_HEIGHT, ALU_WIDTH, TEXT_HEIGHT)
            drawStringCentred(g, rect, tick.toString())
        }
    }

    private fun drawWork(g: Graphics) {
        for ((processorIndex, processor) in system.cpus.withIndex()) {
            processor.alu.timeline.intervals.forEach { interval ->
                g.color = Color.GREEN
                g.fillRect(getALUOffset(processorIndex), HEADER_OFFSET + (interval.startTick - 1) * TIC_HEIGHT, ALU_WIDTH, TIC_HEIGHT * interval.length)
                g.color = Color.BLACK
                g.drawRect(getALUOffset(processorIndex), HEADER_OFFSET + (interval.startTick - 1) * TIC_HEIGHT, ALU_WIDTH, TIC_HEIGHT * interval.length)

                val rect = Rectangle(getALUOffset(processorIndex), HEADER_OFFSET + (interval.startTick - 1) * TIC_HEIGHT, ALU_WIDTH, TIC_HEIGHT * interval.length)
                drawStringCentred(g, rect, interval.work.realId.toString())
            }
            processor.ioUnit.internalLinks.forEachIndexed { linkId, link ->
                link.sendingTimeline.intervals.forEach { interval ->
                    g.color = Color.CYAN
                    g.fillRect(getSendingOffset(processorIndex, linkId), HEADER_OFFSET + (interval.startTick - 1) * TIC_HEIGHT, IO_WIDTH, TIC_HEIGHT * interval.length)
                    g.color = Color.BLACK
                    g.drawRect(getSendingOffset(processorIndex, linkId), HEADER_OFFSET + (interval.startTick - 1) * TIC_HEIGHT, IO_WIDTH, TIC_HEIGHT * interval.length)

                    val rect = Rectangle(getSendingOffset(processorIndex, linkId), HEADER_OFFSET + (interval.startTick - 1) * TIC_HEIGHT, IO_WIDTH, TIC_HEIGHT * interval.length)
                    val transfer = interval.work
                    drawStringCentred(g, rect, "${transfer.completedTask.realId}→${transfer.requiringTask.realId}•${transfer.path.first().realId}→${transfer.path.last().realId}")
                }
                link.receivingTimeline.intervals.forEach { interval ->
                    g.color = Color.ORANGE
                    g.fillRect(getReceivingOffset(processorIndex, linkId), HEADER_OFFSET + (interval.startTick - 1) * TIC_HEIGHT, IO_WIDTH, TIC_HEIGHT * interval.length)
                    g.color = Color.BLACK
                    g.drawRect(getReceivingOffset(processorIndex, linkId), HEADER_OFFSET + (interval.startTick - 1) * TIC_HEIGHT, IO_WIDTH, TIC_HEIGHT * interval.length)

                    val rect = Rectangle(getReceivingOffset(processorIndex, linkId), HEADER_OFFSET + (interval.startTick - 1) * TIC_HEIGHT, IO_WIDTH, TIC_HEIGHT * interval.length)
                    val transfer = interval.work
                    drawStringCentred(g, rect, "${transfer.completedTask.realId}→${transfer.requiringTask.realId}•${transfer.path.first().realId}→${transfer.path.last().realId}")
                }
            }
        }
    }

    private fun drawWorkTotal(g: Graphics) {
        val rect = Rectangle(BORDERS_PADDING, BORDERS_PADDING + ticsCount * TIC_HEIGHT + HEADER_HEIGHT, processorWidth * processorsNumber, (TEXT_HEIGHT * 1.5).toInt())
        val text = "Completed at $ticsCount tics"
        g.drawRect(BORDERS_PADDING, BORDERS_PADDING + ticsCount * TIC_HEIGHT + HEADER_HEIGHT, processorWidth * processorsNumber, (TEXT_HEIGHT * 1.5).toInt())
        drawStringCentred(g, rect, text)
    }

    private fun drawStringCentred(g: Graphics, rect: Rectangle, text: String) {
        // Get the FontMetrics
        val metrics = g.getFontMetrics(font)
        // Determine the X coordinate for the text
        val x = rect.x + (rect.width - metrics.stringWidth(text)) / 2
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        val y = rect.y + (rect.height - metrics.height) / 2 + metrics.ascent
        // Draw the String
        g.drawString(text, x, y)
    }

    private fun getALUOffset(cpuIndex: Int) = BORDERS_PADDING + cpuIndex * processorWidth

    private fun getSendingOffset(cpuIndex: Int, linkIndex: Int) =
        getALUOffset(cpuIndex) + ALU_WIDTH + linkIndex * 2 * IO_WIDTH

    private fun getReceivingOffset(cpuIndex: Int, linkIndex: Int) =
        getALUOffset(cpuIndex) + ALU_WIDTH + linkIndex * 2 * IO_WIDTH + IO_WIDTH
}