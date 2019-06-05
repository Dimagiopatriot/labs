import com.mxgraph.model.mxCell
import com.mxgraph.model.mxGraphModel
import com.mxgraph.view.mxGraph

val mxGraph.cellsCollection
    get() = ((this.model as mxGraphModel).cells as Map<String, mxCell>).values

val mxGraph.reformattedData
    get() = reformatData(cellsCollection)