@Suppress("ArrayInDataClass")
data class ReformattedData(
    val linksMatrix: Array<IntArray>,
    val matrixIdToRealIdMap: Map<Int, Int>,
    val matrixIdToValueMap: Map<Int, Int>,
    val reversedLinksMatrix: Array<IntArray>
) {
    fun toRealId(matrixId: Int) = matrixIdToRealIdMap[matrixId] ?: throw NoSuchElementException()
    fun toMatrixId(realId: Int) = matrixIdToRealIdMap.filterValues { it == realId }.keys.first()
}